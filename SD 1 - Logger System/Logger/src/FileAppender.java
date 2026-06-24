public class FileAppender implements Appender {
    private String filePath;

    public FileAppender(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void append(LogMessage logMessage) {
        System.out.println(logMessage.getMessage());
    }
}
