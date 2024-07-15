package test;

import com.migrsoft.main.Paragraph;
import com.migrsoft.main.StringResources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.awt.*;

public class ParagraphTest {

    private Graphics g;
    private Font font;

    @Before
    public void setup() {
        g = Mockito.mock(Graphics2D.class);
        font = new Font(StringResources.FONT_MAIN, Font.PLAIN, 16);
        g.setFont(font);
    }

    @Test
    public void testLayout() {
        Paragraph p = new Paragraph("这里面有一段中文长句子.", 100, null);
        p.layout();
        for (String s : p.getLines()) {
            System.out.println(s);
        }
    }

    @Test
    public void layoutTest() {
        Paragraph p = new Paragraph("这是一段中文句子，其中还有 English……测试分段", 100, null);
        p.layout(g);
    }
}
