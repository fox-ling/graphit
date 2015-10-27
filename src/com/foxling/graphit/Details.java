/* Graphit - log file browser
 * Copyright© 2015 Shamil Absalikov, foxling@live.com
 *
 * Graphit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graphit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.foxling.graphit;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.JTree;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;

import com.foxling.graphit.LogFile.Startup;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

public class Details extends JFrame {
	private static final long serialVersionUID = 1L;
	private SimpleDateFormat fTime = new SimpleDateFormat ("HH:mm:ss");
	private SimpleDateFormat fDateTime = new SimpleDateFormat ("dd.MM.YYYY HH:mm:ss");
	private String summaryHdr = "";
	
	private String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public Details(final LogFile lf){
		super("Details: " + lf.fileName);
		
		summaryHdr = lf.fileName + LINE_SEPARATOR;
		summaryHdr += "file: "+lf.intFileName + LINE_SEPARATOR;
		summaryHdr += "serial no.: "+lf.serialNo + LINE_SEPARATOR;
		summaryHdr += "counter: "+Integer.toString(lf.counter) + LINE_SEPARATOR;
		
		setBounds(100, 100, 600, 500);
		
		JPanel contentPane = new JPanel();
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		setContentPane(contentPane);
		
		final JTextPane tpLines = new JTextPane();
		tpLines.setEditable(false);
		JScrollPane scrollPane = new JScrollPane();
		final JTextArea lines = new JTextArea("");
		lines.setBackground(Color.LIGHT_GRAY);
		lines.setEditable(false);
		scrollPane.setViewportView(tpLines);
		scrollPane.setRowHeaderView(lines);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		/*
	    
		
	    try {
			String str = "123 Hello there! Check out this awesome green text! "+ System.getProperty("line.separator");
			doc.insertString(doc.getLength(), str, doc.getStyle(str));
			doc.setCharacterAttributes(0, 3, sLineNo, false);
			doc.setCharacterAttributes(40, 5, sError, false);
			doc.insertString(doc.getLength(), str, doc.getStyle(str));
		} catch (BadLocationException ble) {
		    System.err.println("Couldn't insert initial text into text pane.");
		}
	    tpLines.setStyledDocument(doc);
		lines.setText("1"+ System.getProperty("line.separator")+"2");**/
		/*doc.addDocumentListener(new DocumentListener(){
			public String getText(){
				System.out.println(0);
				int caretPosition = textPane.getDocument().getLength();
				Element root = textPane.getDocument().getDefaultRootElement();
				String text = "1" + System.getProperty("line.separator");
				for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
				lines.setText(getText());
				System.out.println(1);
			}
 
			@Override
			public void insertUpdate(DocumentEvent de) {
				lines.setText(getText());
				System.out.println(2);
			}
 
			@Override
			public void removeUpdate(DocumentEvent de) {
				lines.setText(getText());
				System.out.println(3);
			}
 
		});*/
		
		JPanel topleft_panel = new JPanel();
	    SpringLayout sl_tlpanel = new SpringLayout();
	    topleft_panel.setPreferredSize(new Dimension(260,250));
	    topleft_panel.setLayout(sl_tlpanel);		
		
		JPanel panel = new JPanel();
		sl_tlpanel.putConstraint(SpringLayout.NORTH, panel, -70, SpringLayout.SOUTH, topleft_panel);
		sl_tlpanel.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, topleft_panel);
		sl_tlpanel.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, topleft_panel);
		sl_tlpanel.putConstraint(SpringLayout.EAST, panel, 0, SpringLayout.EAST, topleft_panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		topleft_panel.add(panel);
	    
		final JTree tree = new JTree(makeTree(lf));
	    JScrollPane sp_tree = new JScrollPane(tree);
	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    sl_tlpanel.putConstraint(SpringLayout.NORTH, sp_tree, 0, SpringLayout.NORTH, topleft_panel);
	    sl_tlpanel.putConstraint(SpringLayout.WEST, sp_tree, 0, SpringLayout.WEST, topleft_panel);
	    sl_tlpanel.putConstraint(SpringLayout.SOUTH, sp_tree, 0, SpringLayout.NORTH, panel);
	    sl_tlpanel.putConstraint(SpringLayout.EAST, sp_tree, 0, SpringLayout.EAST, topleft_panel);
	    topleft_panel.add(sp_tree);
		
		final JTextPane tpSummary = new JTextPane();
		tpSummary.setText(summaryHdr);
	    tpSummary.setEditable(false);
		JScrollPane scrollPane2 = new JScrollPane(tpSummary);
		
		final JSplitPane topsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topleft_panel, scrollPane2);
		topsplitPane.setResizeWeight(.5);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topsplitPane, scrollPane);
		splitPane.setResizeWeight(.5);
		sl_contentPane.putConstraint(SpringLayout.NORTH, splitPane, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, contentPane);
	    contentPane.add(splitPane);
		
	    final JCheckBox chbCheckAll = new JCheckBox("...");
	    chbCheckAll.setSelected(true);
		sl_panel.putConstraint(SpringLayout.NORTH, chbCheckAll, 5, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, chbCheckAll, 5, SpringLayout.WEST, panel);
		panel.add(chbCheckAll);
		
		final JCheckBox chbWrongHash = new JCheckBox("\u041D\u0435\u043F\u0440\u0430\u0432\u0438\u043B\u044C\u043D\u044B\u0439 \u0445\u044D\u0448");
		chbWrongHash.setSelected(true);
		sl_panel.putConstraint(SpringLayout.NORTH, chbWrongHash, -5, SpringLayout.SOUTH, chbCheckAll);
		sl_panel.putConstraint(SpringLayout.WEST, chbWrongHash, 5, SpringLayout.WEST, panel);
		panel.add(chbWrongHash);
		
		final JCheckBox chbUnparsable = new JCheckBox("\u041D\u0435\u043E\u043F\u0440\u0435\u0434\u0435\u043B\u0435\u043D\u043D\u044B\u0435");
		chbUnparsable.setSelected(true);
		sl_panel.putConstraint(SpringLayout.NORTH, chbUnparsable, -5, SpringLayout.SOUTH, chbWrongHash);
		sl_panel.putConstraint(SpringLayout.WEST, chbUnparsable, 5, SpringLayout.WEST, panel);
		panel.add(chbUnparsable);
		
		chbCheckAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chbWrongHash.setSelected(chbCheckAll.isSelected());
				chbUnparsable.setSelected(chbCheckAll.isSelected());
				refreshDetails(lf, tree.getSelectionModel().getSelectionPath(), tpSummary, lines, tpLines, chbWrongHash.isSelected(), chbUnparsable.isSelected());
			}
		});
		
		chbWrongHash.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chbCheckAll.setSelected(false);
				refreshDetails(lf, tree.getSelectionModel().getSelectionPath(), tpSummary, lines, tpLines, chbWrongHash.isSelected(), chbUnparsable.isSelected());
			}
		});
		
		chbUnparsable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chbCheckAll.setSelected(false);
				refreshDetails(lf, tree.getSelectionModel().getSelectionPath(), tpSummary, lines, tpLines, chbWrongHash.isSelected(), chbUnparsable.isSelected());
			}
		});
		
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
		    @Override
		    public void valueChanged(TreeSelectionEvent e) {
		    	refreshDetails(lf, e.getPath(), tpSummary, lines, tpLines, chbWrongHash.isSelected(), chbUnparsable.isSelected());
		    }
		});
	}

	protected void refreshDetails(LogFile lf, TreePath path
					, JTextPane summary, JTextArea lineno, JTextPane lines
					, boolean showHash, boolean showUnparsable) {
		if (path.getPathCount() == 0) return;
		
		String summarytext = summaryHdr;
		
		int startupID = -1;
		Startup currStartup = null;
		if (path.getPathCount()>1) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) path.getPathComponent(0);
			DefaultMutableTreeNode startup = (DefaultMutableTreeNode) path.getPathComponent(1);
			startupID = root.getIndex(startup);
			
			currStartup = lf.startup.get(startupID);
			summarytext += "--------------------"+LINE_SEPARATOR;
			summarytext += "STARTUP #"+Integer.toString(startupID)+"::"+LINE_SEPARATOR;
			summarytext += "- datetime: " + fDateTime.format(currStartup.datetime)+LINE_SEPARATOR;
			summarytext += "- line no.: "+Integer.toString(currStartup.lineNo)+LINE_SEPARATOR;
			summarytext += "- lines count: "+Integer.toString(currStartup.lines.size())+LINE_SEPARATOR;
		}
		
		lineno.setText("");
		StyleContext sc = new StyleContext();
		final DefaultStyledDocument doc = new DefaultStyledDocument(sc);
		
		for (int i = 0; i < lf.startup.size(); i++) {
			if (startupID == -1) 
				currStartup = lf.startup.get(i);
			
			if (showHash || showUnparsable) {
				int l = -1;
				final Style sError = sc.addStyle("Error", null);
				StyleConstants.setForeground(sError, Color.red);
				final Style sComment = sc.addStyle("Comment", null);
				StyleConstants.setForeground(sComment, Color.lightGray);
				for (int j = 0; j < currStartup.badLines.size(); j++) {
					BadLine currLine = currStartup.badLines.get(j);
					
					if (showHash && currLine.errorID==9 || showUnparsable && currLine.errorID != 9) {					
						lineno.append(Integer.toString(currLine.lineID)+LINE_SEPARATOR);
						
						l = doc.getLength();
						try {
							doc.insertString(l, currLine.line + "  " + currLine.errorMsg + LINE_SEPARATOR, null);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
						doc.setCharacterAttributes(l+currLine.errorPos, currLine.errorLen, sError, false);
						doc.setCharacterAttributes(l+currLine.line.length()+2, currLine.errorMsg.length(), sComment, false);
					}
				}
			}
			lines.setStyledDocument(doc);
			
			if (startupID != -1)
				break;
		}
		
		summary.setText(summarytext);
	}

	private DefaultMutableTreeNode makeTree(LogFile lf) {
		String filename = Paths.get(lf.fileName).getFileName().toString();;
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(filename);
		
		for (int i = 0; i < lf.startup.size(); i++) {
			Startup currStartup = lf.startup.get(i);
			root.add(new DefaultMutableTreeNode("#"+Integer.toString(i)+" "+fTime.format(currStartup.time)));
		}
		
		return root;
	}
}
