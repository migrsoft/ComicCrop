package test;

import com.migrsoft.main.Paragraph;
import org.junit.jupiter.api.Test;

public class ParagraphTest {

    @Test
    public void testLayout() {
        Paragraph p = new Paragraph("这里面有一段中文长句子.", 100, null);
        p.layout();
        for (String s : p.getLines()) {
            System.out.println(s);
        }
    }
}
