/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lsstcorp.etravelerbackendnode;
import java.io.Writer;

/**
 *
 * @author jrb
 */
public class StringArrayWriter extends Writer {
  private String[] m_lines = null;
  private int m_used = 0;
  private int m_fetched = 0;
 
  public StringArrayWriter(int size) {
    m_lines = new String[size];
  }
  public StringArrayWriter() {
   m_lines = new String[100];
  } 
  /**
   * Fill next available String in array m_lines with specified input
   * @param chars   char array containing those to be saved
   * @param off     offset into array of first char to be saved
   * @param len     number of chars to be saved
   */
  public void write(char[] chars, int off, int len) {
    write (new String(chars, off, len));
  }
  
  public void write(String str)  {
    if (m_used == m_lines.length)  { // make bigger array
      String[] tmpLines = new String[2*m_lines.length];
      System.arraycopy(m_lines, 0, tmpLines, 0, m_used);
      m_lines = tmpLines;
    }
    m_lines[m_used] = new String(str);
    m_used++;
  }
  /**
   * Start over with a new internal array the same size as the old one
   */
  public void flush() {
    m_lines = new String[m_lines.length];
    m_used = 0;
    m_fetched = 0;
  }
  public String[] fetchLineArray() {
    return m_lines;
  }
  public String fetchLine(int i) {
    if (i < m_used) return m_lines[i];
    return null;
  }
  public String fetchNext() {
    if (m_fetched >= m_used) return null;
    m_fetched++;
    return m_lines[m_fetched - 1];
  }
  
  public int fetchNUsed() {
    return m_used;
  }
  
  public void close() {
 
  }
}
