public class ConsoleAppender implements Appender{
    @Override
    public void append(LogMessage logMessage) {
        System.out.println(logMessage.getMessage());
    }
}
