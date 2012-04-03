/*
 *  Copyright (c) 2011 . Patrick Hochstenbach <Patrick.Hochstenbach@gmail.com>
 */

package circulationapp;

/**
 *
 * @author hochsten
 */
public class StdoutPrintViewImpl implements PrintView {

    public void setText(String str) {
        System.out.print(str);
    }

    public void append(String str) {
        System.out.print(str);
    }

}
