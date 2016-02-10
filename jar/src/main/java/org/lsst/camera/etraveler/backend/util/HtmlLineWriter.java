/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsst.camera.etraveler.backend.util;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;

/**
 *
 * @author jrb
 */
public class HtmlLineWriter   extends OutputStreamWriter implements LineWriter {
    public HtmlLineWriter(OutputStream ow) {
       super(ow);
    }
    public void writeln(String text) throws IOException {
        write("<p>" + text + "</p>");
    }
}
