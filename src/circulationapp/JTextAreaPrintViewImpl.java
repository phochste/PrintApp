/*
 *  Copyright (c) 2011 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */

package circulationapp;

import java.awt.Rectangle;
import javax.swing.JTextArea;

/**
 *
 * @author hochsten
 */
public class JTextAreaPrintViewImpl implements PrintView {
    private JTextArea jText;
    
    public JTextAreaPrintViewImpl(JTextArea jText) {
        this.jText = jText;
    }

    public void setText(String str) {
        jText.setText(str);
    }

    public void append(String str) {
        jText.append(str);
        jText.scrollRectToVisible(new Rectangle(0,jText.getHeight()-2,1,1));
    }
}
