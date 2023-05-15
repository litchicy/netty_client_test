import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;

public class CountTest {

    @Test
    public void count() throws IOException {
        String filePath = "src/test/resources/test.txt"; // 将文件路径替换成你要处理的文件路径
        File file = new File(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(file));

        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            count += countMessagesInLine(line);
        }
        reader.close();

        System.out.println("该文件中共出现了 " + count + " 个“服务端接受到到了你的消息，并且往 127.0.0.1:6965回复了！！！”字段");
    }

    private static int countMessagesInLine(String line) {
        int count = 0;
        int index = -1;
        while ((index = line.indexOf("服务端接受到到了你的消息，并且往 127.0.0.1:6965回复了！！！", index + 1)) != -1) {
            count++;
        }
        return count;
    }
}
