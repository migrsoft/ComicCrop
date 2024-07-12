package com.migrsoft.main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.filechooser.FileFilter;

import com.migrsoft.image.PicWorkerParam;
import com.migrsoft.main.ProgressDlg.TaskType;

/**
 * @author wuyulun
 *
 */
public class ComicCrop extends JFrame {

	private MenuBarInViewMode viewModeMenuBar;
	private MenuBarInEditMode editModeMenuBar;
	
	private PicEditor editor;
	private PicViewer viewer;
	private PicList list;
	private Dashboard mBoard;
	
	private String lastPath = "";
	private String fileName = "";

	private ZipFile zipFile = null;

	public ComicCrop() {
		super(StringResources.APP_TITLE);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				viewer.saveSubtitles();
				dispose();
				System.exit(0);
			}
		});

		createViewModeMenu();
		createEditModeMenu();
		setJMenuBar(editModeMenuBar.getMenuBar());
		
		createContent();

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		setSize((int)(screen.getWidth() * 0.9f), (int)(screen.getHeight() * 0.9f));
		setLocation((int)(screen.getWidth() - getWidth()) / 2, (int)(screen.getHeight() - getHeight()) / 2);
		setVisible(true);
		setResizable(false);
	}

	private void createViewModeMenu() {
		MenuBarInViewMode.Callback cb = new MenuBarInViewMode.Callback() {
			@Override
			public void onFileOpen() {
				openFile();
			}

			@Override
			public void onFileOpenComic() {
				openComic();
			}

			@Override
			public void onFileSaveSubtitle() {
				viewer.saveSubtitles();
			}

			@Override
			public PicWorkerParam.SubtitleSwitch getSubtitleSwitch() {
				return MainParam.getInstance().getSubtitleSwitch();
			}

			@Override
			public void onSubtitle(PicWorkerParam.SubtitleSwitch value) {
				MainParam.getInstance().setSubtitleSwitch(value);
				repaint();
			}

			@Override
			public boolean getPageSpacingSwitch() {
				return MainParam.getInstance().getPageSpacingSwitch();
			}

			@Override
			public void setSpacingSwitch(boolean value) {
				MainParam.getInstance().setPageSpacingSwitch(value);
			}

			@Override
			public boolean getPageNumberSwitch() {
				return MainParam.getInstance().getPageNumberSwitch();
			}

			@Override
			public void setPageNumberSwitch(boolean value) {
				MainParam.getInstance().setPageNumberSwitch(value);
				repaint();
			}
		};

		viewModeMenuBar = new MenuBarInViewMode(cb);
	}

	private void createEditModeMenu() {
		MenuBarInEditMode.Callback cb = new MenuBarInEditMode.Callback() {
			@Override
			public void onFileOpen() {
				openFile();
			}

			@Override
			public void onFileOpenComic() {
				openComic();
			}

			@Override
			public boolean isLockWindowSize() {
				return isResizable();
			}

			@Override
			public void onLockWindowSize() {
				lockWindow();
			}

			@Override
			public void onLoadTask() {
				readTasks();
			}

			@Override
			public void onSaveTask() {
				saveTasks();
			}

			@Override
			public void onToGrayscale() {
				editor.toGrayMode();
			}

			@Override
			public void onForceGrayCalc(boolean checked) {
				MainParam.getInstance().setForceGray(checked);
				editor.reload();
			}

			@Override
			public void onAutoGrayCalc(boolean checked) {
				MainParam.getInstance().setAutoGrayLevel(checked);
				editor.reload();
			}

			@Override
			public PicWorkerParam.ImageFormat getImageFormat() {
				return MainParam.getInstance().getImageFormat();
			}

			@Override
			public void onImageFormat(PicWorkerParam.ImageFormat format) {
				MainParam.getInstance().setImageFormat(format);
			}

			@Override
			public boolean isCutWhiteEdge() {
				return false;
			}

			@Override
			public void onCutWhiteEdge(boolean value) {

			}

			@Override
			public int getImageSize() {
				return 800;
			}

			@Override
			public void onImageSize(int width, int height) {

			}
		};

		editModeMenuBar = new MenuBarInEditMode(cb);
	}

	private final String menuPicToGray = "转换为灰度图";
	private final String menuPicForceGray = "强制计算灰阶";
	private final String menuPicAutoGrayLevel = "自动计算灰阶";
	private final String menuPicOutputPng = "输出为 PNG 格式";
	private final String menuPicOutputJpeg = "输出为 JPEG 格式";
	private final String menuPicOutputWebp = "输出为 WEBP 格式";
	private final String menuPicCropWhite = "切白边";
	private final String menuPicCropBlack = "切黑边";
	private final String menuPicSize480 = "480 x 800";
	private final String menuPicSize600 = "600 x 1000";
	private final String menuPicSize800 = "800 x 1280";
	
	private final String menuTask = "任务";
	private final String menuTaskSplit = "批量页面分割";
	private final String menuTaskRename = "按序号重命名";
	private final String menuTaskRenamePlus = "高级重命名";
	private final String menuTaskCrop1 = "批量页面操作：旋转→剪裁";
	private final String menuTaskCrop2 = "批量页面操作：旋转→剪裁→缩放";
	private final String menuTaskCrop3 = "批量页面操作：旋转→剪裁→缩放宽度";
	
	private final String menuTaskThread = "线程设置";
	private final String menuTaskThreadUse1 = "1 线程";
	private final String menuTaskThreadUse2 = "4 线程";
	private final String menuTaskThreadUse3 = "8 线程";
	private final String menuTaskThreadUse4 = "12 线程";
	
	private final String menuHelp = "帮助";
	private final String menuHelpGuide = "使用说明";
	private final String menuHelpUpdate = "查看更新";
	private final String menuHelpAbout = "关于";

	private JMenuBar editMenubar;

	private void createMenu() {
		editMenubar = new JMenuBar();
		setJMenuBar(editMenubar);
		
		////////////////////////////////////////////////////////////
		
		ActionListener menuHandler = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				String cmd = event.getActionCommand();
				
				if (cmd.equals(menuPicToGray)) {
					editor.toGrayMode();
				}
				else if (cmd.equals(menuPicForceGray)) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem)event.getSource();
					MainParam.getInstance().setForceGray(item.isSelected());
					editor.reload();
				}
				else if (cmd.equals(menuPicAutoGrayLevel)) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem)event.getSource();
					MainParam.getInstance().setAutoGrayLevel(item.isSelected());
					editor.reload();
				}
				else if (cmd.equals(menuPicOutputPng)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setOutputFormat(MainParam.OUTPUT_FORMAT_PNG);
				}
				else if (cmd.equals(menuPicOutputJpeg)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setOutputFormat(MainParam.OUTPUT_FORMAT_JPG);
				}
				else if (cmd.equals(menuPicOutputWebp)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setOutputFormat(MainParam.OUTPUT_FORMAT_WEBP);
				}
				else if (cmd.equals(menuPicCropWhite)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setCropWhite(true);
					editor.reload();
				}
				else if (cmd.equals(menuPicCropBlack)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setCropWhite(false);
					editor.reload();
				}
				else if (cmd.equals(menuPicSize480)) {
					MainParam.getInstance().setMaxWidth(480);
					MainParam.getInstance().setMaxHeight(800);
				}
				else if (cmd.equals(menuPicSize600)) {
					MainParam.getInstance().setMaxWidth(600);
					MainParam.getInstance().setMaxHeight(1000);
				}
				else if (cmd.equals(menuPicSize800)) {
					MainParam.getInstance().setMaxWidth(800);
					MainParam.getInstance().setMaxHeight(1280);
				}
				else if (cmd.equals(menuTaskSplit)) {
					batchSplitWork();
				}
				else if (cmd.equals(menuTaskRename)) {
					batchRenameWork();
				}
				else if (cmd.equals(menuTaskRenamePlus)) {
					batchRenamePlusWork();
				}
				else if (cmd.equals(menuTaskCrop1)) {
					batchCropWork();
				}
				else if (cmd.equals(menuTaskCrop2)) {
					batchCropAndScaleWork();
				}
				else if (cmd.equals(menuTaskCrop3)) {
					batchCropAndScaleWidthWork();
				}
				else if (cmd.equals(menuHelpGuide)) {
					ManualDlg dlg = new ManualDlg(ComicCrop.this);
					dlg.setVisible(true);
				}
				else if (cmd.equals(menuHelpUpdate)) {
					try {
						URI url = new URI("http://pan.baidu.com/share/link?shareid=185881&uk=2299302813");
						Desktop.getDesktop().browse(url);
					}
					catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
				else if (cmd.equals(menuHelpAbout)) {
					JOptionPane.showMessageDialog(null,
							"ComicCrop Version " + StringResources.VERSION + "\n2012-2024 © Woosoft",
							"About",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else if (cmd.equals(menuTaskThreadUse1)) {
					MainParam.getInstance().setMaxThreads(1);
				}
				else if (cmd.equals(menuTaskThreadUse2)) {
					MainParam.getInstance().setMaxThreads(4);
				}
				else if (cmd.equals(menuTaskThreadUse3)) {
					MainParam.getInstance().setMaxThreads(8);
				}
				else if (cmd.equals(menuTaskThreadUse4)) {
					MainParam.getInstance().setMaxThreads(12);
				}
			}
		};

		////////////////////////////////////////////////////////////
		
		JMenu image = new JMenu(StringResources.MENU_IMAGE);
		editMenubar.add(image);
		
		JMenuItem pic_gray = new JMenuItem(menuPicToGray);
		pic_gray.addActionListener(menuHandler);
		
		JCheckBoxMenuItem pic_forceGray = new JCheckBoxMenuItem(menuPicForceGray, MainParam.getInstance().isForceGray());
		pic_forceGray.addActionListener(menuHandler);
		
		JCheckBoxMenuItem pic_autoGrayLevel = new JCheckBoxMenuItem(menuPicAutoGrayLevel, MainParam.getInstance().isAutoGrayLevel());
		pic_autoGrayLevel.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_formatPng = new JRadioButtonMenuItem(menuPicOutputPng);
		pic_formatPng.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_formatJpg = new JRadioButtonMenuItem(menuPicOutputJpeg);
		pic_formatJpg.addActionListener(menuHandler);

		JRadioButtonMenuItem pic_formatWebp = new JRadioButtonMenuItem(menuPicOutputWebp);
		pic_formatWebp.addActionListener(menuHandler);

		ButtonGroup formatGroup = new ButtonGroup();
		formatGroup.add(pic_formatPng);
		formatGroup.add(pic_formatJpg);
		formatGroup.add(pic_formatWebp);

		switch (MainParam.getInstance().getOutputFormat()) {
			case MainParam.OUTPUT_FORMAT_PNG:
				pic_formatPng.setSelected(true);
				break;
			case MainParam.OUTPUT_FORMAT_JPG:
				pic_formatJpg.setSelected(true);
				break;
			case MainParam.OUTPUT_FORMAT_WEBP:
				pic_formatWebp.setSelected(true);
				break;
		}

		JRadioButtonMenuItem pic_cropWhite = new JRadioButtonMenuItem(menuPicCropWhite);
		pic_cropWhite.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_cropBlack = new JRadioButtonMenuItem(menuPicCropBlack);
		pic_cropBlack.addActionListener(menuHandler);
		
		ButtonGroup cropGroup = new ButtonGroup();
		cropGroup.add(pic_cropWhite);
		cropGroup.add(pic_cropBlack);
		
		if (MainParam.getInstance().isCropWhite())
			pic_cropWhite.setSelected(true);
		else
			pic_cropBlack.setSelected(true);
		
		JRadioButtonMenuItem pic_size480 = new JRadioButtonMenuItem(menuPicSize480);
		pic_size480.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_size600 = new JRadioButtonMenuItem(menuPicSize600);
		pic_size600.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_size800 = new JRadioButtonMenuItem(menuPicSize800);
		pic_size800.addActionListener(menuHandler);
		
		ButtonGroup sizeGroup = new ButtonGroup();
		sizeGroup.add(pic_size480);
		sizeGroup.add(pic_size600);
		sizeGroup.add(pic_size800);
		
		pic_size800.setSelected(true);
		
		image.add(pic_gray);
		image.addSeparator();
		image.add(pic_forceGray);
		image.add(pic_autoGrayLevel);
		image.addSeparator();
		image.add(pic_formatPng);
		image.add(pic_formatJpg);
		image.add(pic_formatWebp);
		image.addSeparator();
		image.add(pic_cropWhite);
		image.add(pic_cropBlack);
		image.addSeparator();
		image.add(pic_size480);
		image.add(pic_size600);
		image.add(pic_size800);

		////////////////////////////////////////////////////////////
		
		JMenu task = new JMenu(menuTask);
		editMenubar.add(task);
		
		JMenuItem task_batchSplit = new JMenuItem(menuTaskSplit);
		task_batchSplit.addActionListener(menuHandler);
		
		JMenuItem task_batchRename = new JMenuItem(menuTaskRename);
		task_batchRename.addActionListener(menuHandler);

		JMenuItem task_batchRenamePlus = new JMenuItem(menuTaskRenamePlus);
		task_batchRenamePlus.addActionListener(menuHandler);
		
		JMenuItem task_batchCrop2 = new JMenuItem(menuTaskCrop1);
		task_batchCrop2.addActionListener(menuHandler);
		
		JMenuItem task_batchCrop3 = new JMenuItem(menuTaskCrop2);
		task_batchCrop3.addActionListener(menuHandler);
		
		JMenuItem task_batchCrop4 = new JMenuItem(menuTaskCrop3);
		task_batchCrop4.addActionListener(menuHandler);

		
		JMenu taskThread = new JMenu(menuTaskThread);
		
		JRadioButtonMenuItem taskThread_1 = new JRadioButtonMenuItem(menuTaskThreadUse1);
		taskThread_1.addActionListener(menuHandler);
		JRadioButtonMenuItem taskThread_2 = new JRadioButtonMenuItem(menuTaskThreadUse2);
		taskThread_2.addActionListener(menuHandler);
		JRadioButtonMenuItem taskThread_3 = new JRadioButtonMenuItem(menuTaskThreadUse3);
		taskThread_3.addActionListener(menuHandler);
		JRadioButtonMenuItem taskThread_4 = new JRadioButtonMenuItem(menuTaskThreadUse4);
		taskThread_4.addActionListener(menuHandler);
		
		taskThread.add(taskThread_1);
		taskThread.add(taskThread_2);
		taskThread.add(taskThread_3);
		taskThread.add(taskThread_4);
		
		ButtonGroup threadGroup = new ButtonGroup();
		threadGroup.add(taskThread_1);
		threadGroup.add(taskThread_2);
		threadGroup.add(taskThread_3);
		threadGroup.add(taskThread_4);
		
		switch (MainParam.getInstance().getMaxThreads()) {
		case 1:
			taskThread_1.setSelected(true);
			break;
		case 4:
			taskThread_2.setSelected(true);
			break;
		case 8:
			taskThread_3.setSelected(true);
			break;
		case 12:
			taskThread_4.setSelected(true);
			break;
		}
		
		task.add(task_batchSplit);
		task.addSeparator();
		task.add(task_batchRename);
		task.add(task_batchRenamePlus);
		task.addSeparator();
		task.add(task_batchCrop2);
		task.addSeparator();
		task.add(task_batchCrop3);
		task.addSeparator();
		task.add(task_batchCrop4);
		task.addSeparator();
		task.add(taskThread);
		
		////////////////////////////////////////////////////////////
		
		JMenu help = new JMenu(menuHelp);
		editMenubar.add(help);

		JMenuItem help_manual = new JMenuItem(menuHelpGuide);
		help_manual.addActionListener(menuHandler);
		
		JMenuItem help_update = new JMenuItem(menuHelpUpdate);
		help_update.addActionListener(menuHandler);
		
		JMenuItem help_about = new JMenuItem(menuHelpAbout);
		help_about.addActionListener(menuHandler);
		
		help.add(help_manual);
		help.add(help_update);
		help.addSeparator();
		help.add(help_about);
	}
	
	private void createContent() {
		
		editor = new PicEditor();
		editor.setActListener(new PicEditor.ActListener() {
			
			@Override
			public void loadPrevItem() {
				list.loadPrevItem();
			}
			
			@Override
			public void loadNextItem() {
				list.loadNextItem();
			}

			@Override
			public void cropChanged(boolean isWhite) {
				mBoard.setCropType(isWhite);
			}

			@Override
			public int[] getCropValues() {
				int[] vs = new int[2];
				vs[0] = mBoard.getCropTop();
				vs[1] = mBoard.getCropBottom();
				return vs;
			}
		});

		viewer = new PicViewer();
		viewer.setActListener(new PicViewer.PicViewerCallback() {
			@Override
			public ZipFile getZip() {
				return zipFile;
			}

			@Override
			public String getPath() {
				return lastPath;
			}

			@Override
			public String getFileName() {
				return fileName;
			}

			@Override
			public int getCurrentIndex() {
				return list.getSelectedIndex();
			}

			@Override
			public void setCurrentIndex(int index) {
				list.setSelectedIndex(index);
				updateTitle();
			}

			@Override
			public String getNameByIndex(int index) {
				return list.getStringByIndex(index);
			}
		});
		
		list = new PicList();
		list.setActListener(new PicList.ActListener() {
			
			@Override
			public void onSelect(String name) {
				updateTitle();
				if (zipFile != null) {
					viewer.load(name);
				} else {
					editor.load(lastPath + name);
				}
			}

			@Override
			public void onDelete(Vector<String> names) {
				updateTitle();
				for (String n : names) {
					File f = new File(lastPath + n);
					f.delete();
				}
				resetEditor();
			}

			@Override
			public void onRemove() {
				updateTitle();
				resetEditor();
			}
		});
		
		mBoard = new Dashboard(editor.isCropMode());
		mBoard.setHorizontalSplit(editor.isHorizonalSplit());
		mBoard.setSplitNum(editor.getSplitNum());
		mBoard.setLeftToRight(editor.isLeftToRight());
		mBoard.setActListener(new Dashboard.ActListener() {
			
			@Override
			public void onClockwise(boolean ctrlPressed) {
				editor.rotate(true, ctrlPressed);
			}
			
			@Override
			public void onAntiClockwise(boolean ctrlPressed) {
				editor.rotate(false, ctrlPressed);
			}

			@Override
			public void onSave() {
				editor.save(null);
			}

			@Override
			public void onScale() {
				editor.scale(false);
			}

			@Override
			public void onScaleWidth() {
				editor.scale(true);
			}

			@Override
			public void onOptionChanged() {
				boolean hori = mBoard.isHorizontalSplit();
				boolean ltor = mBoard.isLeftToRight();
				int num = mBoard.getSplitNum();
				editor.setSplitInfo(hori, num, ltor);
			}

			@Override
			public void onModeChanged(boolean isCrop) {
				if (isCrop)
					editor.useCropMode();
				else
					editor.useSplitMode();
			}

			@Override
			public void onSplit() {
				editor.split();
			}

			@Override
			public void onCalcCrop() {
				editor.calcCrop();
			}

			@Override
			public void onCrop() {
				editor.crop();
			}

			@Override
			public void onCropChanged(boolean isWhite, boolean isAll) {
				editor.cropChanged(isWhite, isAll);
			}

			@Override
			public void onGrowArea() {
				editor.growArea(10);
			}

			@Override
			public void onGrowAreaSlightly() {
				editor.growArea(2);
			}
		});
		
		Container contentPane = getContentPane();
		contentPane.add(list.getPane(), BorderLayout.WEST);
	}
	
	private void updateTitle() {
		int index = list.getSelectedIndex() + 1;
		int total = list.getTotal();
		setTitle(StringResources.APP_TITLE + " " + index + " | " + total);
	}
	
	private HashMap<String, TaskData> taskInfo = null;

	private void closeZipFile() {
		viewer.saveSubtitles();
		try {
			if (zipFile != null) {
				zipFile.close();
				zipFile = null;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void switchUI(boolean viewMode) {
		Container container = getContentPane();
		if (viewMode) {
			container.remove(editor);
			container.remove(mBoard);
			container.add(viewer, BorderLayout.CENTER);
		} else { // to edit mode
			container.remove(viewer);
			container.add(editor, BorderLayout.CENTER);
			container.add(mBoard, BorderLayout.EAST);
		}
		container.revalidate();
		container.repaint();
	}

	private void openFile() {
		JFileChooser dlg = new JFileChooser();
		dlg.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				String name = f.getName().toLowerCase();
				return name.endsWith(".jpg")
						|| name.endsWith(".jpeg")
						|| name.endsWith(".png")
						|| name.endsWith(".webp")
						|| f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "图像文件";
			}
			
		});
		dlg.setMultiSelectionEnabled(true);
		if (lastPath != null) {
			dlg.setCurrentDirectory(new File(lastPath));
		} else {
			String currentPath = System.getProperty("user.dir");
			dlg.setCurrentDirectory(new File(currentPath));
		}
		int r = dlg.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			closeZipFile();
			switchUI(false);

			File[] fl = dlg.getSelectedFiles();
			int pathLen = fl[0].getPath().length();
			int nameLen = fl[0].getName().length();
			
			lastPath = fl[0].getPath().substring(0, pathLen - nameLen);
			Vector<String> taskList = new Vector<String>();
			taskInfo = new HashMap<String, TaskData>();
			
			for (File f : fl) {
				taskList.add(f.getName());
			}

			taskList.sort(new SortByName());
			list.update(taskList);

			resetEditor();
		}
	}

	private void openComic() {
		JFileChooser dlg = new JFileChooser();
		dlg.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				String name = f.getName().toLowerCase();
				return name.endsWith(".cbz")
						|| f.isDirectory();
			}

			@Override
			public String getDescription() {
				return StringResources.STR_COMIC_FILES;
			}

		});
		dlg.setMultiSelectionEnabled(false);
		if (lastPath != null) {
			dlg.setCurrentDirectory(new File(lastPath));
		} else {
			String currentPath = System.getProperty("user.dir");
			dlg.setCurrentDirectory(new File(currentPath));
		}
		int r = dlg.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			closeZipFile();
			switchUI(true);

			File fl = dlg.getSelectedFile();
			int pathLen = fl.getPath().length();
			int nameLen = fl.getName().length();

			lastPath = fl.getPath().substring(0, pathLen - nameLen);
			fileName = fl.getName();
			int dotIndex = fileName.lastIndexOf('.');
			if (dotIndex > 0) {
				fileName = fileName.substring(0, dotIndex);
			}

			Vector<String> taskList = new Vector<String>();
			taskInfo = new HashMap<String, TaskData>();

			try {
				ZipInputStream zis = new ZipInputStream(new FileInputStream(fl));
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					taskList.add(entry.getName());
					zis.closeEntry();
				}

				zipFile = new ZipFile(fl.getPath());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			taskList.sort(new SortByName());
			list.update(taskList);

			viewer.reset();
			viewer.loadSubtitles();
			viewer.repaint();
		}
	}

	public void openPath(String path) {
		
		File p = new File(path);
		String[] all = p.list();

		lastPath = path;
		Vector<String> taskList = new Vector<String>();
		taskInfo = new HashMap<String, TaskData>();
		
		String ext = MainParam.getInstance().getOutputExtName();
        assert all != null;
        for (String s : all) {
			File f = new File(path + s);
			if (f.isFile() && !f.isHidden()) {
				if (f.getName().endsWith(ext)) {
					taskList.add(f.getName());
				}
			}
		}
		
		taskList.sort(new SortByName());
		list.update(taskList);
		
		resetEditor();
		
		mBoard.useCropMode();
		editor.useCropMode();
	}

	private void lockWindow() {
		boolean b = isResizable();
		b = !b;
		setResizable(b);
	}
	
	private final String TASK_FILE_NAME = "comic_tasks.dat";
	
	@SuppressWarnings("unchecked")
	private void readTasks() {
		if (JOptionPane.showConfirmDialog(
				this, "是否读取保存的工作？", "读取确认", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		
		try {
			FileInputStream fs = new FileInputStream(TASK_FILE_NAME);
			ObjectInputStream os = new ObjectInputStream(fs);
			Object tasks = os.readObject();
			os.close();
			if (tasks instanceof HashMap<?, ?>) {
				taskInfo = (HashMap<String, TaskData>)tasks;
				editor.reload();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void saveTasks() {
		if (taskInfo == null)
			return;
		
		if (JOptionPane.showConfirmDialog(
				this, "是否保存现在工作？",
				"保存确认", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		
		editor.saveTaskData();
		try {
			FileOutputStream fs = new FileOutputStream(TASK_FILE_NAME);
			ObjectOutputStream os = new ObjectOutputStream(fs);
			os.writeObject(taskInfo);
			os.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void clearTempDir(String path) {

		File p = new File(path);
		if (!p.exists())
			return;
		
		String[] all = p.list();
		if (all == null)
			return;
		
		if (all.length > 0) {
			if (JOptionPane.showConfirmDialog(
					this, "是否清空 " + path + " 文件夹？",
					"删除确认", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
		}
		
		String ext = MainParam.getInstance().getOutputExtName();
		for (String s : all) {
			File f = new File(path + s);
			if (f.isFile() && !f.isHidden()) {
				if (f.getName().endsWith(ext))
					f.delete();
			}
		}
	}
	
	private boolean useMultiThreads(int size) {
		int max = MainParam.getInstance().getMaxThreads();
		if (max == 1 || size < max)
			return false;
		else
			return true;
	}
	
	private void batchSplitWork() {
		Vector<String> taskList = list.getList();
		if (!taskList.isEmpty()) {
			editor.saveTaskData();
			
			clearTempDir(lastPath + editor.getSaveDir());
			
			ProgressDlg dlg = new ProgressDlg(this);
			
			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, taskInfo, TaskType.TASK_SPLIT,
						lastPath, editor.getSaveDir(),
						editor.isHorizonalSplit(), editor.isLeftToRight(), editor.getSplitNum(),
						0, 0);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, taskInfo);
				
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_SPLIT,
						lastPath, editor.getSaveDir(),
						editor.isHorizonalSplit(), editor.isLeftToRight(), editor.getSplitNum(),
						0, 0);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}

	private void renameAndUpdateList(Vector<RenameThem.Item> result) {
		Vector<String> newList = list.getList();
		for (int i=0, j=0; i < result.size(); i++) {
			RenameThem.Item rti = result.get(i);
			File f1 = new File(lastPath + rti.getOriginName());
			File f2 = new File(lastPath + rti.getNewName());
			if (f1.renameTo(f2)) {
				for (; j < newList.size(); j++) {
					String item = newList.get(j);
					if (item.equals(rti.getOriginName())) {
						newList.set(j, rti.getNewName());
						j++;
						break;
					}
				}
			}
		}
		list.update(newList);
	}
	
	private void batchRenameWork() {
		Vector<String> list = this.list.getListSelected();
		if (list.isEmpty()) {
			list = this.list.getList();
		}
		if (!list.isEmpty()) {
			if (JOptionPane.showConfirmDialog(
					this, "是否重命名列表中的图片？",
					"确认", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
			
			String prefix = JOptionPane.showInputDialog("输入文件名前缀");

			RenameThem work = new RenameThem();
			work.initial(list);
			work.rename(prefix, 1, 3);
			renameAndUpdateList(work.getResult());
			resetEditor();
		}
	}

	private void batchRenamePlusWork() {
		Vector<String> list = this.list.getListSelected();
		if (list.isEmpty()) {
			list = this.list.getList();
		}
		if (!list.isEmpty()) {
			final RenamePlusDlg dlg = new RenamePlusDlg();
			dlg.setData(list, lastPath);
			dlg.setMyActionListener(new RenamePlusDlg.MyActionListener() {
				@Override
				public void onRename() {
					renameAndUpdateList(dlg.getResults());
					resetEditor();
				}
			});
			dlg.setVisible(true);
		}
	}
	
	private void batchCropWork() {
		Vector<String> taskList = list.getList();
		if (!taskList.isEmpty()) {
			editor.saveTaskData();
			
			ProgressDlg dlg = new ProgressDlg(this);
			int maxw = MainParam.getInstance().getMaxWidth();
			int maxh = MainParam.getInstance().getMaxHeight();

			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, taskInfo, TaskType.TASK_CROP,
						lastPath, null, false, false, 0, maxw, maxh);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, taskInfo);
	
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_CROP,
						lastPath, null, false, false, 0, maxw, maxh);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}
	
	private void batchCropAndScaleWork() {
		Vector<String> taskList = list.getList();
		if (!taskList.isEmpty()) {
			editor.saveTaskData();
			
			ProgressDlg dlg = new ProgressDlg(this);
			int maxw = MainParam.getInstance().getMaxWidth();
			int maxh = MainParam.getInstance().getMaxHeight();

			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, taskInfo, TaskType.TASK_CROP_SCALE,
						lastPath, null, false, false, 0, maxw, maxh);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, taskInfo);
	
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_CROP_SCALE,
						lastPath, null, false, false, 0, maxw, maxh);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}
	
	private void batchCropAndScaleWidthWork() {
		Vector<String> taskList = list.getList();
		if (!taskList.isEmpty()) {
			editor.saveTaskData();
			
			ProgressDlg dlg = new ProgressDlg(this);
			int maxw = MainParam.getInstance().getMaxWidth();
			int maxh = MainParam.getInstance().getMaxHeight();

			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, taskInfo, TaskType.TASK_CROP_SCALE_WIDTH,
						lastPath, null, false, false, 0, maxw, maxh);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, taskInfo);
	
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_CROP_SCALE_WIDTH,
						lastPath, null, false, false, 0, maxw, maxh);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}
	
	public TaskData loadTaskData(String path) {
		String name = getNameFromPath(path);
		if (taskInfo.containsKey(name))
			return taskInfo.get(name);
		return null;
	}
	
	public void saveTaskData(String path, TaskData data) {
		String name = getNameFromPath(path);
        taskInfo.remove(name);
		taskInfo.put(name, data);
	}
	
	private String getNameFromPath(String path) {
		return path.substring(lastPath.length());
	}
	
	public void clearTaskInfo() {
		taskInfo.clear();
	}
	
	public void resetEditor() {
		editor.reset();
		editor.repaint();
	}
	
	static private ComicCrop sInstance = null;
	
	static public ComicCrop getInstance() {
		return sInstance;
	}

	private static void accessCounter() {
		Runnable checkin = new Runnable() {

			@Override
			public void run() {
				try {
					URL url = new URL("http://pan.baidu.com/share/link?shareid=185765&uk=2299302813");
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestProperty("Accept-Encoding", "gzip");
					conn.connect();
					int code = conn.getResponseCode();
					System.out.println("http: " + code);
                } catch (Exception e) {
					System.out.println(e.getMessage());
				}
				finally {
					System.out.println("Welcome to ComicCrop!");
				}
			}
		};
		new Thread(checkin).start();
	}

	private static void listAvailableFonts() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		// Get all font family names
		String[] fontNames = ge.getAvailableFontFamilyNames();

		// Print all font family names
		System.out.println("Available Fonts:");
		for (String fontName : fontNames) {
			System.out.println(fontName);
		}
	}

	public static void main(String[] args) {

//		listAvailableFonts();

		Runnable r = new Runnable() {
			public void run() {
				sInstance = new ComicCrop();
			}
		};
		EventQueue.invokeLater(r);
	}
}
