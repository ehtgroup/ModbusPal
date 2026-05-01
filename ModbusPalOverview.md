# ModbusPal Architectural Overview

ModbusPal is a NetBeans-built Modbus slave simulator with a Swing GUI tightly coupled to its domain logic. ~14K LOC across 114 Java files in 12 packages under `src/modbuspal/`.

## Entry Point

`src/modbuspal/main/ModbusPalGui.java` parses CLI args and dispatches:

| Flag | Behavior |
|------|----------|
| (none) | Launches Swing GUI (`ModbusPalFrame` containing `ModbusPalPane`) |
| `-install` | Installs RXTX serial native libs, exits |
| `-hide` | Headless: loads project, starts all automations + TCP link, sleeps forever |
| `-loadFile=<path>` | Initial project file (absolute path) |
| `-portNumber=<n>` | Override TCP port (else uses project default or 502) |
| `-learn` | Enables learn mode on the loaded project |
| `-help` | Prints usage and exits |

`ModbusPalGui` itself is mostly static helpers + two inner frame classes (`ModbusPalFrame` JFrame, `ModbusPalInternalFrame` JInternalFrame). The real UI lives in `ModbusPalPane`.

## Package Layout

| Package | Role | Central classes |
|---------|------|-----------------|
| `main` | Orchestration, project model, UI shell | `ModbusPalProject`, `ModbusPalPane`, `ModbusPalGui`, `ModbusPalXML` |
| `slave` | Modbus device models + register stores | `ModbusSlave`, `ModbusRegisters`, `ModbusCoils`, `ModbusPduProcessor`, `ModbusSlaveAddress` |
| `link` | Transport layer | `ModbusLink`, `ModbusTcpIpLink`, `ModbusSerialLink`, `ModbusReplayLink`, `ModbusSlaveProcessor`, `ModbusTcpIpSlaveDispatcher` |
| `automation` | Threaded time-stepped value generation | `Automation`, `AutomationPanel`, `AutomationChart` |
| `generator` | Per-step value functions (linear/random/sine subdirs) | `Generator` (abstract base) |
| `binding` | Maps automation output → register/coil values | `Binding`, `Binding_SINT16`, `Binding_SINT32`, `Binding_FLOAT32` |
| `script` | Jython integration | `ScriptRunner`, `PythonGenerator`, `PythonBinding` |
| `recorder` | Traffic capture | `ModbusPalRecorder`, `ModbusPalRecord` |
| `master` | Outbound request testing (peripheral) | `ModbusMasterDialog`, `ModbusMasterRequest`, `ModbusMasterTask` |
| `instanciator` | Reflection-based plugin registry | `Instantiable<T>`, `InstantiableManager<T>` |
| `help` | Embedded HTML help viewer | `HelpViewer` |
| `toolkit` | Utilities (XML, CRC, hex, UI widgets) | `XMLTools`, `ModbusTools`, `Jythools`, `NumericTextField` |

## Core Runtime Objects

### `ModbusPalProject` vs `ModbusPalProject2`

- **`ModbusPalProject2`** (326 LOC, abstract base) — holds only the `HashMap<ModbusSlaveAddress, ModbusSlave>` plus the `forwardToLocalHost` Docker flag. Contains commented-out address-matching code (TCP/RTU fallback logic).
- **`ModbusPalProject`** (2153 LOC, extends Project2) — everything else: automations, scripts, master tasks, plugin factories, link config, XML load/save.

The split appears to be an unfinished refactor — Project2 adds no useful seam currently, and the commented-out matching logic hints at an in-progress Docker/network-resilience feature that was never finished.

### `ModbusPalPane` vs `ModbusPalGui`

- `ModbusPalPane` (2004 LOC, extends `JPanel`) — the actual UI. Wraps a `ModbusPalProject`, listens to its events (`ModbusPalListener`), manages tabs for slaves, automations, scripts, link config, master dialog.
- `ModbusPalGui` — just hosts `ModbusPalPane` inside frames and holds a static `HashMap` of named instances (`getInstance(key)` factory pattern for embedding multiple panes).

## Request Flow (TCP)

When a Modbus client sends a request to the listening port:

1. `ModbusTcpIpLink.run()` accepts the socket, spawns a `ModbusTcpIpSlaveDispatcher`.
2. Dispatcher reads the request PDU, calls inherited `ModbusSlaveProcessor.processPDU()`.
3. `processPDU()` checks slave enabled, applies optional no-reply error rate, looks up the `ModbusSlave` via `project.getModbusSlave(addr)`, fetches the `ModbusPduProcessor` for the function code, invokes it, applies the configured reply delay, records traffic.
4. The PDU processor (e.g. FC3 "Read Holding Registers") calls `slave.getHoldingRegister(addr)` for each requested address.
5. `ModbusRegisters.getValue(addr)` — if a `Binding` is attached, delegates to `binding.getRegister()`; otherwise returns the static stored value.
6. `Binding.getRegister()` reads the bound `Automation`'s current value and converts it (per type: SINT16 / SINT32 / FLOAT32 split across register order).

### The value pipeline

```
Automation (Thread, steps at stepDelay)
    → Generator (per-step function: linear/random/sine/python)
    → fires automationValueHasChanged event
    → Binding (listener) converts double → register int / coil bool
    → ModbusRegisters / ModbusCoils
    → returned in PDU response
```

Automations run as their own threads. Bindings are `AutomationExecutionListener`s attached to specific register addresses.

## Project File Format

`.xmpp` files — XML with DTD validation (`modbuspal.dtd` loaded from resources by `XMLTools`). Top-level elements:

- `<automation>` — step delay, loop flag, ordered `<generator>` children
- `<generator class="...">` — duration + subclass-specific `<param>` children, instantiated reflectively
- `<binding automation="..." class="SINT16|SINT32|FLOAT32" order="...">` — attached to register addresses
- `<slave id="...">` with `<holding_registers>`, `<functions>`, `<tuning>` (delays, error rates)
- `<link type="tcp|serial|replay" .../>`
- `<script type="beforeinit|afterinit|ondemand" file="...">`

`ModbusPalProject.load(File)` parses via `XMLTools.ParseXML()`, instantiates plugins through factories, runs `BEFORE_INIT` scripts, then loads parameters. Save is symmetrical. XML constants live in `ModbusPalXML`.

## Headless Mode (`-hide`)

`ModbusPalGui.main()` lines 228–254:

1. Resolve `-loadFile=` path, verify it exists.
2. `loadProject(path)` → `ModbusPalProject.load(file, true)`.
3. Force `forwardToLocalHost = true` (Docker plumbing).
4. Apply `-learn` flag if present.
5. `startAll()` — starts every automation thread, then `startTcpIpLink()` opens the listening socket on the project's configured port (or 502 fallback).
6. Infinite `Thread.sleep(1000)` loop to keep the JVM alive.

No Swing frames are constructed. Useful for Docker/CI deployments.

## Plugin System

`instanciator.InstantiableManager<T>` is a reflective factory registry used for **three** plugin axes:

1. **Generators** — registered linear/random/sine + dynamically loaded Python classes
2. **Bindings** — SINT16/SINT32/FLOAT32 + Python
3. **PDU processors** — built-in Modbus function codes + custom user code

Plugins implement `Instantiable<T>`, get registered by class name, and are serialized by class name in the XML project file. Jython scripts can register new plugins at runtime.

## Vestigial / Suspicious Code

- **`ModbusPalProject2`** — the split adds no real abstraction; appears to be an extraction-in-progress for a feature that was never finished. The commented-out IP/RTU address-matching code points at a Docker resilience refactor.
- **`forwardToLocalHost`** Docker flag — partially wired, default-true only in headless mode.
- **`master/` package** — outbound request simulator, peripheral to the slave-simulation purpose; useful for testing peer devices but not core.
- **`PythonBinding` / `PythonGenerator`** — thin stubs that exist to be subclassed by Jython scripts at load time.

## Build & Dependencies

- **`build.xml`** — Ant, NetBeans-generated, imports `nbproject/build-impl.xml`. Custom `-post-init` runs Ivy retrieve; `-post-clean` clears the Ivy cache.
- **`ivy.xml`** — runtime dependencies:
  - `jython-standalone` 2.7.0 — Python scripting
  - `jfreechart` 1.0.15 — `AutomationChart`
  - `rxtx` 2.2.2 — serial port (`ModbusSerialLink`)
- **`Dockerfile`** — Alpine JDK16 + Ant + Ivy, builds in-place for headless CI/runtime.
- **`manifest.mf`** — minimal; jar metadata mostly auto-generated by Ant.

## Key Source File Sizes

| File | LOC | Notes |
|------|-----|-------|
| `main/ModbusPalProject.java` | 2153 | Domain model monolith |
| `main/ModbusPalPane.java` | 2004 | UI monolith |
| `main/ModbusPalProject2.java` | 326 | Likely dead-weight base class |
| `main/ModbusPalGui.java` | 421 | Entry point + frame factories |

The architecture is pragmatic (listener + factory patterns) but lacks layering between UI and simulation logic. Refactoring would mean extracting listeners and plugin factories out of the two big classes.
