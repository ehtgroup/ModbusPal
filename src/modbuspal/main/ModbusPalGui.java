/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModbusPalFrame.java
 *
 * Created on 22 oct. 2010, 10:48:15
 */

package modbuspal.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.automation.Automation;
import modbuspal.automation.AutomationPanel;
import modbuspal.link.ModbusSerialLink;
import modbuspal.link.ModbusTcpIpLink;
import org.xml.sax.SAXException;

/**
 * Utilitary methods for creating new instances of ModbusPal
 * @author nnovic
 */
public class ModbusPalGui
{

    private static final HashMap<Object,ModbusPalPane> instances = new HashMap<Object,ModbusPalPane>();
    public static final int MAX_PORT_NUMBER = 65536;
    
    private static String initialLoadFilePath = "";
    private static ModbusPalProject modbusPalProject = null;
    private static int initialPortNumber = -1;    
    
    /**
     * This method will display the help message to the console and exit the software.
     */
    public static void displayHelpMessage()
    {
        System.out.println( "This software launches the Modbus Slave simulation program: ModbusPal." );
        System.out.println( "Arguments in this program include:" );
        System.out.println(
                "-install (optional): This flag is to tell program to install itself if it is not installed." );
        System.out.println( "-loadFile (optional): This argument is to load a project file at launch. Example usage:" );
        System.out.println( "\t-loadFile=\"your/absolute/path/name/example.xmpp\"" );
        System.out.println( "Make sure the path name is the absolute path, or you will get an error message back." );
        System.out.println(
                "-portNumber (optional): This argument sets the initial TCP/IP port number to a number between 0 and "
                        + MAX_PORT_NUMBER + ". Example usage:" );
        System.out.println( "\t-portNumber=1234" );
        System.out.println(
                "Make sure the port you choose is not a reserved port number or in use. If a number is not given, an error message will be returned." );
        System.out.println(
                "If this argument is not given or an invalid port value is given, then the port number will be set to "
                        + ModbusPalPane.DEFAULT_PORT_TEXT + ", or the value in the initial project file loaded." );
        System.out.println( "-help (optional): Displays the help message." );
    	System.exit( 0 );
    }
    
    /**
     * This method gets the initial load file path to load specified by the user in the command line arguments.
     * @return {String} The absolute initial load file path. Returns "" if no argument was given.
     */
    public static String getInitialLoadFilePath()
    {
    	return initialLoadFilePath;
    }
    
    /**
     * This method gets the initial port number to load specified by the user in the command line arguments.
     * @return {int} The initial port number for TCP/IP connections. Returns -1 if no port number was given.
     */
    public static int getInitialPortNumber()
    {
    	return initialPortNumber;
    }
    
    public static void startAllAutomations()
    {
        System.out.printf("[%s] Start all automations\r\n", modbusPalProject.getName());
        Automation automations[] = modbusPalProject.getAutomations();
        for(int i=0; i<automations.length; i++)
        {
            automations[i].start();
        }
    }
    
    public static void startAll()
    {
        System.out.printf("[%s] Start everything\r\n",modbusPalProject.getName());
        startAllAutomations();
        startTcpIpLink();
    }
    
        /**
     * Starts the currently selected link, as if the user has clicked
     * on the "Run" button.
     */
    private static void startTcpIpLink()
    {
        int port = 502;
        String strPort = modbusPalProject.linkTcpipPort;
        try 
        {
            port = Integer.parseInt(strPort);
        } 
        catch (NumberFormatException e) 
        {
            System.out.println("invalid port set, defaulting to 502");
        }
        try
        {
            System.out.printf("[%s] Start TCP/link (port=%d)\r\n", modbusPalProject.getName(), port);
            ModbusTcpIpLink currentLink = new ModbusTcpIpLink(modbusPalProject, port);
            currentLink.start();
        }
        catch (Exception ex)
        {
            System.out.println("TCP/IP error");
            System.out.println("The following exception occured:" + ex.getClass().getSimpleName() + "\r\n");
            System.out.println("Message:"+ex.getLocalizedMessage());
        }
    }
    

    /**
     * this method will try to change the Look and Feel of the application,
     * using the system l&f. It means that the application will get the Windows
     * l&f on Windows, etc...
     */
    private static void setNativeLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e)
        {
          System.out.println("Error setting native LAF: " + e);
        }
    }

    
    public static void install()
    {
        ModbusSerialLink.install();
    }
    
    /**
    * @param {String[]} args The command line arguments
    */
    public static void main(String args[]) 
    {
        boolean runInstall = false;
        boolean runGui = true;
        boolean showUI = true;
        boolean enableLearn = false;
        
        String installArgFlag = "-install";
        String loadFileArgFlag = "-loadFile=";
        String portNumberArgFlag = "-portNumber=";
        String hideArgFlag  = "-hide";
        String enableLearnFlag  = "-learn";
        
        if( args.length >= 1 )
        {
            for(String arg:args)
            {
                if( arg.startsWith( installArgFlag ) )
                {
                    runInstall = true;
                    runGui = false;
                }
                else if( arg.startsWith( hideArgFlag ) )
                {
                    showUI = false;
                    runGui = false;
                }
                else if( arg.startsWith( loadFileArgFlag ) )
                {
                	initialLoadFilePath = arg.substring( arg.lastIndexOf( loadFileArgFlag ) + loadFileArgFlag.length() );
                }
                else if( arg.startsWith( portNumberArgFlag ) )
                {
                	String portNumberString = arg.substring( arg.lastIndexOf( portNumberArgFlag ) + portNumberArgFlag.length() );
                	initialPortNumber = Integer.valueOf( portNumberString ).intValue();
                }
                else if( arg.startsWith( enableLearnFlag ) )
                {
                	enableLearn = true;
                }
                else
                {
                	displayHelpMessage();
                }
            }
        }
        
        if( runInstall == true )
        {
            install();
        }
        
        if( runGui == true )
        {	
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setNativeLookAndFeel();
                    newFrame().setVisible(true);
                }
            });
        }
        else if (showUI == false) {
            String initialLoadProjectFilePath = ModbusPalGui.getInitialLoadFilePath();
            File fileCheck = new File( initialLoadProjectFilePath );
            if( initialLoadProjectFilePath != "" && fileCheck.isFile() )
            {
                try
                {
                    System.out.println( "Loading the project file: " + initialLoadProjectFilePath );
                    modbusPalProject = loadProject( initialLoadProjectFilePath );
                    modbusPalProject.forwardToLocalHost = true;
                    modbusPalProject.setLearnModeEnabled(enableLearn);
                    startAll();
                    while (true) 
                    {
                        Thread.sleep(1000);
                    }
                }
                catch( Exception exception )
                {
                    System.out.println(exception.toString());
                    exception.printStackTrace();
                    System.out.println(
                            "Could not load the initial project file path \"" + initialLoadProjectFilePath + "\"." );
                    System.out.println( "Check the path you inputted into the command line arguments." );
                }
            }
        }
    }

    /**
     * A JinternalFrame that contains a ModbusPalPane.
     */
    public static class ModbusPalInternalFrame
    extends JInternalFrame
    implements InternalFrameListener
    {
        final ModbusPalPane modbusPal;

        /**
         * Creates a new instance of ModbusPalInternalFrame
         */
        public ModbusPalInternalFrame()
        {
            setTitle(ModbusPalPane.APP_STRING);
            setIconImage();
            setLayout( new BorderLayout() );
            modbusPal = new ModbusPalPane(false);
            add( modbusPal, BorderLayout.CENTER );
            pack();
            addInternalFrameListener(this);
        }

        private void setIconImage()
        {
            URL url2 = getClass().getClassLoader().getResource("modbuspal/main/img/icon.png");
            Image image2 = getToolkit().createImage(url2);
            setFrameIcon( new ImageIcon(image2) );
        }

        @Override
        public void internalFrameOpened(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            modbusPal.exit();
        }

        @Override
        public void internalFrameClosed(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameIconified(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameDeiconified(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
        }

        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {
        }
    }

    /**
     * A JFrame with a ModbusPalPane inside
     */
    public static class ModbusPalFrame
    extends JFrame
    {
        final ModbusPalPane modbusPal;

        /**
         * Creates a new instance of ModbusPalFrame
         */
        public ModbusPalFrame()
        {
            setTitle(ModbusPalPane.APP_STRING);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setIconImage();
            setLayout( new BorderLayout() );
            modbusPal = new ModbusPalPane(true);
            add( modbusPal, BorderLayout.CENTER );
            pack();
        }

        private void setIconImage()
        {
            URL url2 = getClass().getClassLoader().getResource("modbuspal/main/img/icon.png");
            Image image2 = getToolkit().createImage(url2);
            setIconImage(image2);
        }
    }
    
    public static ModbusPalProject loadProject(String path)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        File projectFile = new File(path);
        return loadProject(projectFile);
    }
    
    public static ModbusPalProject loadProject(File projectFile)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        ModbusPalProject mpp = ModbusPalProject.load(projectFile, true);  
        return mpp;
    }    
   


    /**
     * Creates a ModbusPalFrame. The internal
     * console of ModbusPal is enabled.
     * @return a new ModbusPalFrame
     */
    public static JFrame newFrame()
    {
        ModbusPalFrame frame = new ModbusPalFrame();
        return frame;
    }

    /**
     * Creates a ModbusPalInternalFrame. The internal
     * console of ModbusPal is enabled.
     * @return a new ModbusPalInternalFrame
     */
    public static JInternalFrame newInternalFrame()
    {
        ModbusPalInternalFrame iframe = new ModbusPalInternalFrame();
        return iframe;
    }

    /**
     * Creates a ModbusPalPane instance. The internal
     * console of ModbusPal is disabled.
     * @return a new ModbusPalPane instance
     */
    public static ModbusPalPane newInstance()
    {
        return new ModbusPalPane(false);
    }


    /** 
     * Returns a ModbusPalPane instance that is associated with
     * the specified key, as in a HashMap. If there is no ModusPalPane
     * associated with that key, a new one is created. Otherwise, the existing
     * one is returned
     * @param key any object that can be used to uniquely identified a particular
     * ModbusPalPane instance. usually a String.
     * @return The ModbusPalPane instance identified by the key
     */
    public static ModbusPalPane getInstance(Object key)
    {
        if( key==null )
        {
            throw new NullPointerException();
        }

        if( instances.containsKey(key)==false )
        {
            instances.put(key, new ModbusPalPane(false) );
        }

        return instances.get(key);
    }


}
