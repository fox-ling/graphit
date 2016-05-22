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

package ru.foxling.graphit.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import ru.foxling.graphit.Core;
import ru.foxling.graphit.logfile.LogFile;
import ru.foxling.graphit.logfile.BadRecord;
import ru.foxling.graphit.logfile.Startup;

import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;

import java.nio.file.Paths;
import java.util.ArrayList;
import net.miginfocom.swing.MigLayout;
import javax.swing.BoxLayout;

public class DetailsFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private JTree tree;
	private JTextPane iSummary;
	private JCheckBox iCheckAll;
	private JCheckBox iWrongHash;
	private JCheckBox iUnparsable;
	private LogFile logFile;
	private JTextPane iLine;
	private JTextPane iLineNo;
	
	public DetailsFrame(final LogFile logFile){
		super("Details" + logFile.getFileName());
		setBounds(100, 100, 600, 500);
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		
		this.logFile = logFile;
		
		iLine = new JTextPane();
		iLine.setEditable(false);
		JScrollPane spLog = new JScrollPane();
		
		iLineNo = new JTextPane();
		iLineNo.setBackground(Color.LIGHT_GRAY);
		iLineNo.setEditable(false);
		
		spLog.setViewportView(iLine);
		spLog.setRowHeaderView(iLineNo);
		spLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel pnlTopLeft = new JPanel();
	    pnlTopLeft.setPreferredSize(new Dimension(260,250));
		pnlTopLeft.setLayout(new MigLayout("insets 2", "[grow]", "[grow]2[]0[]0[]"));
		
	    iCheckAll = new JCheckBox("...");
	    iCheckAll.setSelected(true);
	    iCheckAll.setMargin(new Insets(0, 0, 0, 0));
	    iCheckAll.addActionListener(e -> {
	    		iWrongHash.setSelected(iCheckAll.isSelected());
	    		iUnparsable.setSelected(iCheckAll.isSelected());
	    		refreshDetails();
	    });
	    pnlTopLeft.add(iCheckAll, "cell 0 1");
	    
		iWrongHash = new JCheckBox("Неправильный хэш");
		iWrongHash.setSelected(true);
		iWrongHash.setMargin(new Insets(0, 0, 0, 0));
		iWrongHash.addActionListener(e -> {
			iCheckAll.setSelected(false);
			refreshDetails();
		});
		pnlTopLeft.add(iWrongHash, "cell 0 2");
		
		iUnparsable = new JCheckBox("Неопределенные");
		iUnparsable.setSelected(true);
		iUnparsable.setMargin(new Insets(0, 0, 0, 0));
		iUnparsable.addActionListener(e -> {
			iCheckAll.setSelected(false);
			refreshDetails();
		});
		pnlTopLeft.add(iUnparsable, "cell 0 3");
		
	    
		tree = new JTree(makeTree(logFile));
	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    JScrollPane spTree = new JScrollPane(tree);
	    pnlTopLeft.add(spTree, "cell 0 0,grow");
		
		iSummary = new JTextPane();
		iSummary.setText(getSummaryHeader());
	    iSummary.setEditable(false);
		JScrollPane spSummary = new JScrollPane(iSummary);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		final JSplitPane spltTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlTopLeft, spSummary);
		spltTop.setResizeWeight(.5);
		
		JSplitPane spltVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spltTop,
				spLog);
				//spRecords);
		spltVertical.setResizeWeight(.5);
		contentPane.add(spltVertical);
		
		tree.getSelectionModel().addTreeSelectionListener(e -> refreshDetails());
	}
	
	private String getSummaryHeader() {
		StringBuilder text = new StringBuilder(250);
		text.append(logFile.getFileName()).append(LINE_SEPARATOR)
			.append("file: ").append(logFile.getIntFileName()).append(LINE_SEPARATOR)
			.append("serial no.: ").append(logFile.getSerialNo()).append(LINE_SEPARATOR)
			.append("counter: ").append(logFile.getCounter()).append(LINE_SEPARATOR);
		return text.toString();
	}

	protected void refreshDetails() {
		TreePath path = tree.getSelectionModel().getSelectionPath();
		if (path.getPathCount() == 0) return;
		
		ArrayList<Startup> startups;
		StringBuilder summarytext = new StringBuilder(getSummaryHeader());
		if (path.getPathCount() > 1) {
			startups = new ArrayList<>(1);
			DefaultMutableTreeNode tnStartup = (DefaultMutableTreeNode) path.getPathComponent(1);
			StartupNode startupNode = (StartupNode) tnStartup.getUserObject();
			
			Startup startup = startupNode.getStartup(); 
			summarytext.append("--------------------").append(LINE_SEPARATOR)
				.append("STARTUP #").append(startupNode.getId()).append("::").append(LINE_SEPARATOR)
				.append("- datetime: ").append(startup.getDatetime().format(Core.F_DATETIME)).append(LINE_SEPARATOR)
				.append("- line no.: ").append(startup.getLineNo()).append(LINE_SEPARATOR);
				//.append("- lines count: ").append(startup.getRecords().size()).append(LINE_SEPARATOR)

			startups.add(startup);
		} else
			startups = logFile.getStartups();
		
		iSummary.setText(summarytext.toString());
		
		StyleContext
				scLineNo = new StyleContext(),
				scLine = new StyleContext();
		StyledDocument
				dLine = new DefaultStyledDocument(scLine),
				dLineNo = new DefaultStyledDocument(scLineNo);
		Style sAlignRight = scLineNo.getStyle(StyleContext.DEFAULT_STYLE),
				sError = scLine.addStyle("Error", null),
				sComment = scLine.addStyle("Comment", null);
		StyleConstants.setAlignment(sAlignRight, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setForeground(sError, Color.red);
		StyleConstants.setForeground(sComment, Color.lightGray);
		
		if (iWrongHash.isSelected() || iUnparsable.isSelected()) {
			TIntIntHashMap index = logFile.getIndex();
			TIntIntProcedure p = new TIntIntProcedure() {
				@Override
				public boolean execute(int key, int value) {
					int l, offset, length;
					String errorMsg;
					if (iWrongHash.isSelected() && value > 0) {
						offset = -1;
						length = -1;
						errorMsg = "Некоррекная хэш сумма строки";
					} else
						if (iUnparsable.isSelected() && value < 0) {
							BadRecord rec = logFile.getBadRecords().get(-1 * value - 1);
							offset = rec.getErrorOffset();
							length = rec.getErrorLength();
							errorMsg = rec.getErrorMsg();
						} else
							return true;
					
					String srcLine = logFile.getSourceLine(key);
					l = dLine.getLength();
					try {
						dLine.insertString(l, srcLine + "  " + errorMsg + LINE_SEPARATOR, null);
						dLineNo.insertString(dLineNo.getLength(), Integer.toString(0/*rec.getLineno()*/) + LINE_SEPARATOR, sAlignRight);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					
					if (offset != -1) dLine.setCharacterAttributes(l + offset, length, sError, false);
					dLine.setCharacterAttributes(l + srcLine.length() + 2, errorMsg.length(), sComment, false);
					return true;
				}
			};
			index.forEachEntry(p);
		}
		iLineNo.setStyledDocument(dLineNo);
		iLine.setStyledDocument(dLine);
	}

	private DefaultMutableTreeNode makeTree(LogFile lf) {
		String filename = Paths.get(lf.getFileName()).getFileName().toString();;
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(filename);
		
		for (int i = 0; i < lf.getStartups().size(); i++)
			root.add(new DefaultMutableTreeNode(new StartupNode(i, lf.getStartups().get(i))));
		
		return root;
	}
	
	private class StartupNode {
		private final int id;
		private final String caption;
		private final Startup startup;
		
		public StartupNode(int id, Startup startup) {
			this.id = id;
			this.startup = startup;
			this.caption = String.format("#%d %s", id, startup.getTime().format(Core.F_TIME));
		}
		
		public int getId() { return id; }
		public Startup getStartup() { return startup; }
		public String toString() { return caption; }
	}
}
