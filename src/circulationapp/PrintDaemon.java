/*
 *  Copyright (c) 2011 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */

package circulationapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 *
 * @author hochsten
 */
public class PrintDaemon implements FileAlterationListener  {
    private PrintView view;
    private File hotFolder;
    private FileAlterationMonitor monitor;
    private boolean demo = true;
    private int interval = 10000;
    private int printer = -1;
    private boolean running = false;

    public PrintDaemon(PrintView view) {
        this.view = view;
        init();
    }

    public void init() {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("usergui.properties");
            setHotFolder(new File(config.getString("daemon.hotfolder", "spool")));
            setDemo(config.getBoolean("daemon.demo", true));
            setPrinter(config.getInt("daemon.printer", 0));
            setInterval(config.getInt("daemon.interval", 10000));

            if (config.getBoolean("daemon.running")) {
                start();
            }
            
        } catch (ConfigurationException ex) {
            Logger.getLogger(PrintDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        File directory = getHotFolder();
        FileAlterationObserver observer = new FileAlterationObserver(directory);
        observer.addListener(this);
        monitor = new FileAlterationMonitor(getInterval());
        monitor.addObserver(observer);

        try {
            monitor.start();
            running = true;
            showInfo();
        } catch (Exception ex) {
            Logger.getLogger(PrintDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        try {
            monitor.stop();
            running = false;
            showInfo();
        } catch (Exception ex) {
            Logger.getLogger(PrintDaemon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setPrinter(int printer) {
        this.printer = printer;
    }

    public int getPrinter() {
        return printer;
    }

    public void setHotFolder(File hotFolder) {
        this.hotFolder = hotFolder;
    }

    public File getHotFolder() {
        return hotFolder;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    public boolean isDemo() {
        return demo;
    }

    public boolean isRunning() {
        return running;
    }
    
    protected void showInfo() {
        PrintService[] services = getServices();

        if (services.length > 0) {
            //DocPrintJob printJob = services[0].createPrintJob();
            //Doc document = new SimpleDoc(pdfBytes, flavor, null)
            //printJob.print(document, null);

            view.setText(services.length + " PDF services:\n");

            for (int i = 0 ; i < services.length ; i++) {
                if (i == getPrinter()) {
                    view.append("  " + i + ". " + services[i].getName() + " [selected]\n");
                }
                else {
                    view.append("  " + i + ". " + services[i].getName() + "\n");
                }
            }
        }
        else {
            view.setText("! No PDF services available\n");
        }

        if (getHotFolder() != null) {
            view.append("Hot Folder: " + getHotFolder() + "\n");
        }
        else {
            view.append("Hot Folder: <undefined>\n");
        }

        if (isDemo()) {
            view.append("Demo mode: On\n");
        }
        else {
            view.append("Demo mode: Off\n");
        }

        view.append("Interval: " + getInterval()/1000 + " seconds\n");
        view.append("Daemon running: " + isRunning());
    }

    public PrintService[] getServices() {
        DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
        return services;
    }

    public void printFile(File file) {
        DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
        PrintService[] services = getServices();

        if (getPrinter() >= 0 && getPrinter() < services.length) {
            try {
                Date date = new Date();

                view.append(date + ": printing " + file + "...");
                FileInputStream fin = new FileInputStream(file);

                PrintService ps = services[getPrinter()];
                DocPrintJob printJob = ps.createPrintJob();

                Doc document = new SimpleDoc(fin, flavor, null);

                if (! isDemo() ) {
                    printJob.print(document, null);
                }

                fin.close();

                view.append("done\n");
            } catch (PrintException ex) {
                Logger.getLogger(PrintDaemon.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrintDaemon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        PrintDaemon p = new PrintDaemon(new StdoutPrintViewImpl());
        p.setPrinter(0);
        p.setHotFolder(new File("/tmp"));
        p.setDemo(true);
     //   p.setInterval(60000);
        p.showInfo();
        p.start();
    }

    public void onStart(FileAlterationObserver fao) {
       // view.append("Scanning: " + fao.getDirectory() + "\n");
    }

    public void onDirectoryCreate(File file) {
       // view.append(" * new directory: " + file + "\n");
    }

    public void onDirectoryChange(File file) {
       // view.append(" * change directory: " + file + "\n");
    }

    public void onDirectoryDelete(File file) {
       // view.append(" * delete directory: " + file + "\n");
    }

    public void onFileCreate(File file) {
       // view.append(" * file create: " + file + "\n");
        if (file.getName().endsWith(".pdf")) {
            printFile(file);
            FileUtils.deleteQuietly(file);
        }
        else {
            view.append("! " + file + " is not a .pdf file\n");
        }
    }

    public void onFileChange(File file) {
       // view.append(" * file change: " + file + "\n");
    }

    public void onFileDelete(File file) {
       // view.append(" * file delete: " + file + "\n");
    }

    public void onStop(FileAlterationObserver fao) {
       // view.append(" * hot folder stopping: " + fao.getDirectory() + "\n");
    }
}