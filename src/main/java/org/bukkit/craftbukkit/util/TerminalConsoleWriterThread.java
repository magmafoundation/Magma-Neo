package org.bukkit.craftbukkit.util;

public class TerminalConsoleWriterThread /*extends Thread*/ {/* // Paper - Comment out entire class
                                                             private final ResourceBundle bundle = ResourceBundle.getBundle(CandidateListCompletionHandler.class.getName(), Locale.getDefault());
                                                             private final ConsoleReader reader;
                                                             private final OutputStream output;
                                                             private volatile int completion = -1;
                                                             
                                                             public TerminalConsoleWriterThread(OutputStream output, ConsoleReader reader) {
                                                             super("TerminalConsoleWriter");
                                                             this.output = output;
                                                             this.reader = reader;
                                                             
                                                             this.setDaemon(true);
                                                             }
                                                             
                                                             @Override
                                                             public void run() {
                                                             String message;
                                                             
                                                             // Using name from log4j config in vanilla jar
                                                             while (true) {
                                                             message = LogQueues.getNextLogEvent("TerminalConsole");
                                                             if (message == null) {
                                                             continue;
                                                             }
                                                             
                                                             try {
                                                             if (Main.useJline) {
                                                             reader.print(Ansi.ansi().eraseLine(Erase.ALL).toString() + ConsoleReader.RESET_LINE);
                                                             reader.flush();
                                                             output.write(message.getBytes());
                                                             output.flush();
                                                             
                                                             try {
                                                             reader.drawLine();
                                                             } catch (Throwable ex) {
                                                             reader.getCursorBuffer().clear();
                                                             }
                                                             
                                                             if (completion > -1) {
                                                             // SPIGOT-6705: Make sure we print the display line again on tab completion, so that the user does not get stuck on it
                                                             reader.print(String.format(bundle.getString("DISPLAY_CANDIDATES"), completion));
                                                             }
                                                             
                                                             reader.flush();
                                                             } else {
                                                             output.write(message.getBytes());
                                                             output.flush();
                                                             }
                                                             } catch (IOException ex) {
                                                             Logger.getLogger(TerminalConsoleWriterThread.class.getName()).log(Level.SEVERE, null, ex);
                                                             }
                                                             }
                                                             }
                                                             
                                                             void setCompletion(int completion) {
                                                             this.completion = completion;
                                                             }
                                                             */
    // Paper - Comment out entire class
}
