# ModbusPal

NetBeans-built Swing GUI Modbus slave simulator. Java, Ant + Ivy build. ~14K LOC, 114 files under `src/modbuspal/`. See `ModbusPalOverview.md` for the full architectural writeup.

## Entry point

`src/modbuspal/main/ModbusPalGui.java` — CLI parser + frame factory.

Flags: `-install`, `-hide` (headless), `-loadFile=<abs path>`, `-portNumber=<n>`, `-learn`, `-help`.

Headless mode: loads project, sets `forwardToLocalHost=true`, calls `startAll()` (every automation thread + TCP link), infinite sleep.

## Core model

UI and domain are tightly coupled. Two monoliths to know:

- **`main/ModbusPalProject.java`** (2153 LOC) — the project model: slaves, automations, scripts, master tasks, plugin factories, link config, XML I/O. Extends `ModbusPalProject2`.
- **`main/ModbusPalProject2.java`** (326 LOC) — abstract base with just the slave HashMap + `forwardToLocalHost`. Looks like an unfinished refactor extraction; commented-out IP/RTU address-matching code suggests an in-progress Docker resilience feature. Don't assume the split is meaningful.
- **`main/ModbusPalPane.java`** (2004 LOC, JPanel) — the actual UI. `ModbusPalGui` just hosts it inside frames.

## Request flow (TCP)

```
ModbusTcpIpLink.run()
  → ModbusTcpIpSlaveDispatcher (per-connection)
  → ModbusSlaveProcessor.processPDU()
  → ModbusSlave.getPduProcessor(FC).processPDU()
  → ModbusRegisters.getValue(addr)
  → if bound: Binding.getRegister()  → reads Automation's current value
```

## Value pipeline

```
Automation (own Thread, steps at stepDelay)
  → Generator (linear / random / sine / python) produces a double
  → fires automationValueHasChanged event
  → Binding (SINT16 / SINT32 / FLOAT32 / python) converts to register int or coil bool
  → ModbusRegisters / ModbusCoils
```

## Plugins

`instanciator/InstantiableManager<T>` is a reflective factory registry used for three axes: **generators**, **bindings**, **PDU processors** (custom Modbus function codes). Plugins are serialized by class name in `.xmpp` XML and reflectively instantiated on load. Jython scripts can register plugins at runtime via `script/ScriptRunner`.

## Project files

`.xmpp` — XML with DTD validation (`modbuspal.dtd`, loaded from resources). Tags defined in `main/ModbusPalXML.java`. Parsed/written via `toolkit/XMLTools`.

## Packages

- `main` — orchestration, project, UI shell
- `slave` — `ModbusSlave`, register/coil stores, `ModbusPduProcessor`
- `link` — `ModbusTcpIpLink`, `ModbusSerialLink`, `ModbusReplayLink`, `ModbusSlaveProcessor`
- `automation` — `Automation` (Thread), `AutomationChart` (JFreeChart)
- `generator` — `Generator` base + linear/random/sine subdirs
- `binding` — value→register conversion
- `script` — Jython glue (`PythonGenerator`, `PythonBinding` are stubs subclassed by user scripts)
- `recorder`, `master` (outbound, peripheral), `instanciator`, `help`, `toolkit`

## Build

Ant (`build.xml`, NetBeans-generated, imports `nbproject/build-impl.xml`). Ivy pulls deps via `-post-init`.

`ivy.xml` deps: `jython-standalone 2.7.0`, `jfreechart 1.0.15`, `rxtx 2.2.2` (serial native libs need `-install`).

`Dockerfile`: Alpine JDK16 + Ant + Ivy, in-place build for headless deployment.

## Things to watch out for

- `ModbusPalProject2` may look pointless — it currently is. Don't add to it without knowing whether the planned-but-unfinished split is being completed or undone.
- `master/` package is outbound testing, not core slave simulation.
- `forwardToLocalHost` (Docker plumbing) is partially wired — only forced true in headless mode.
- UI listeners and domain logic are interleaved across `ModbusPalProject` / `ModbusPalPane`; changes in one frequently require touching the other.
