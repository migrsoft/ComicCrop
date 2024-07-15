package test;

import com.migrsoft.main.SubtitleItem;
import com.migrsoft.main.SubtitleManager;
import org.junit.Test;

public class SubtitleManagerTest {

    private static final String name = "/Users/joba/temp/test.st";

    @Test
    public void write() {
        SubtitleItem si = new SubtitleItem();
        si.rect.setBounds(5,5, 100, 100);
        si.originalText = "English";
        si.translatedText = "Chinese";
        si.originalTextFontSize = 12;
        si.translatedTextFontSize = 12;
        SubtitleManager manager = new SubtitleManager();
        manager.addSubtitle("test", si);
        manager.save(name);
    }

    @Test
    public void read() {
        SubtitleManager manager = new SubtitleManager();
        manager.load(name);
    }
}
