package org.lsst.camera.etraveler.backend.hnode;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

// interfaces which apply to both XSSF (xlsx) and HSSF (xls) documents
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.WorkbookFactory;

// Expect underlying format will be xlsx
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.lsst.camera.etraveler.backend.exceptions.EtravelerException;
import org.lsst.camera.etraveler.backend.util.Verify;

public class HardwareAssemblyTable implements HardwareTypeNode.Importer  {
  public static final int FTYPE_XLSX=1;

  private  static ArrayList<String> s_keys=null;
  private  int m_currentRow = -1;
  private  int m_nRow = 0;
  private  ConcurrentHashMap<String, Integer> m_keyIndices;
  private  ConcurrentHashMap<Integer, String> m_indexKey = null;
  private  ArrayList< ConcurrentHashMap<String, String> >  m_table = null;

  public HardwareAssemblyTable() {
    init();
  }
  public HardwareAssemblyTable(String filepath, int ftype)
    throws EtravelerException {
    init();
    readXlsx(filepath);
    
  }
  public String provideName() {
    if (m_currentRow < 0) return null;
    return m_table.get(m_currentRow).get("name");
  }
  
   public String provideSlotname() {
    if (m_currentRow < 0) return null;
    return m_table.get(m_currentRow).get("slotname");
  }
  public boolean provideIsBatched() throws EtravelerException {
    if (m_currentRow < 0) {
      throw new EtravelerException("HardwareAssemblyTable: bad row");
    }
    String s =  m_table.get(m_currentRow).get("isBatched");
    if (s.equals("True") || s.equals("true") || s.equals("yes") 
      || s.equals("Yes") ) return true;
    return false;
  }
  public int provideQuantity() throws EtravelerException {
    if (m_currentRow < 0) {
      throw new EtravelerException("HardwareAssemblyTable: bad row");
    }
    int q;
    try {
      String s = m_table.get(m_currentRow).get("quantity");
      q = Integer.parseInt(s);
    } catch (NumberFormatException e) {
      throw new EtravelerException("HardwareAssemblyTable: bad quantity");
    }
    if (q < 1) {
      throw new EtravelerException("HardwareAssemblyTable: bad quantity");
    }
    return q;
  }
  public String provideComments() {
    if (m_currentRow < 0) return null;
    return m_table.get(m_currentRow).get("comments");
  }
  public int provideNChildren() throws EtravelerException  {
    if (m_currentRow < 0) return 0;
    int rowIx = m_currentRow + 1;
    int nChildren = 0;
    int nextLevel;
    try {
      nextLevel = 1 + Integer.parseInt(m_table.get(m_currentRow).get("level"));
    } catch (NumberFormatException ex) {
      throw new EtravelerException("HardwareAssemblyTable: bad level");
    } 
    while (rowIx < m_nRow) {
      int level;
      try {  
        level = Integer.parseInt(m_table.get(m_currentRow).get("level"));
      } catch (NumberFormatException ex) {
        throw new EtravelerException("HardwareAssemblyTable: bad level");
      }
      if (level == nextLevel)  {
        nChildren++;
        rowIx++;  
      }
      else return nChildren;
    }
    return nChildren;
  }
  public void finishFetchAttributes() {
    m_currentRow++;
  }
  public HardwareTypeNode provideNextChild(HardwareTypeNode parent) 
  throws Exception {
    return new HardwareTypeNode(parent, this);
  }
  public boolean finishImport() {
    return (m_currentRow == m_nRow);
  }
  public void reset() {
    if (m_nRow > 0) {
      m_currentRow = 0;
    } else {
      m_currentRow = -1;
    }
  }
  private  void init() {
    if (s_keys==null) {
      initKeys();
    }
    m_keyIndices = new ConcurrentHashMap<String, Integer>();
    m_indexKey = new ConcurrentHashMap<Integer, String>();
  }

  private static void initKeys() {
    s_keys = new ArrayList<String>();
    s_keys.add("Level");
    s_keys.add("Name");
    s_keys.add("Slotname");
    s_keys.add("Batched");
    s_keys.add("Quantity");
    s_keys.add("Comments");
  }

  private void readXlsx(String fp) throws EtravelerException {
    FileInputStream fstream = null;

    try {
      fstream = new FileInputStream(fp);
    } catch (FileNotFoundException e) {
      System.out.println("No such file as " + fp + "\n");
      return;
    }
    Workbook wb = null;
    try {
      wb = WorkbookFactory.create(fstream);
    } catch (InvalidFormatException e) {
      System.out.println("Invalid spreadsheet input\n");
      return;
    } catch (IOException ex) {
      System.out.println("IOException detected\n");
      return;
    }
    interpret(wb);
  }

  /**
     Find first row containing first key value.  Check that it
     also contains all the keys; save column positions.
     If so, read in values in those columns for successive rows,
     rejecting any with null or empty values in essential columns.
   */
  private void interpret(Workbook wb) throws EtravelerException {
    Sheet s = wb.getSheet("eTraveler");
    if (s == null) {
      s = wb.getSheetAt(0);
    }
    if (s == null) {
      throw new EtravelerException("Workbook has no sheets");
    }
    // Scan rows, looking for one containing the string "Level"
    Iterator<Row> rows = s.rowIterator();
    Row row = null;
    int rowNum = 0;
    int ix1 = -1;
    while (rows.hasNext() && ix1 == -1) {
      row = rows.next();
      System.out.print("On row ");  System.out.println(rowNum);
      rowNum++;
      if (row == null) continue;
      ix1 = findIx(row, s_keys.get(0));
      if (ix1 < 0) continue;
      m_keyIndices.put(s_keys.get(0), ix1);
      m_indexKey.put(ix1, s_keys.get(0));
      // Assume this is header row and make sure it contains all other keys
      for (int kIx=1; kIx < s_keys.size(); kIx++) {
        int ix = findIx(row, s_keys.get(kIx));
        if (ix < 0) {
          throw new EtravelerException("Incomplete header row");
        }
        m_keyIndices.put(s_keys.get(kIx), ix);
        m_indexKey.put(ix, s_keys.get(kIx));
      }
    }
    if (ix1 < 0) {
      throw new EtravelerException("No header row found");
    }
    /* Now save info from remaining rows */
    while (rows.hasNext() ) {
      row = rows.next();
      rowNum++;
      if (row == null) continue;
      ConcurrentHashMap<String, String> ourRow =
        new ConcurrentHashMap<String, String>();
      for (int ix: m_indexKey.keySet()) {
        Cell cell = row.getCell(ix);
        if (cell == null) continue;
        ourRow.put(m_indexKey.get(ix), cell.getStringCellValue());
      }
      m_table.add(ourRow);
    }
    
  }
  private int findIx(Row row, String key) {
    short minColIx = row.getFirstCellNum();
    short maxColIx = row.getLastCellNum();

    for (int ix = minColIx; ix <= maxColIx; ix++) {
      Cell cell = row.getCell(ix);
      if (cell == null) continue;
      if (cell.getStringCellValue().equals(key)) return ix;
    }
    return -1;
  }

}
