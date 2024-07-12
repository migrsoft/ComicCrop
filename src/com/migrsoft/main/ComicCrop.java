package com.migrsoft.main;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
		setJMenuBar(viewModeMenuBar.getMenuBar());
		
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

			@Override
			public void onManual() {
				onMenuManual();
			}

			@Override
			public void onAbout() {
				onMenuAbout();
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
				MainParam.getInstance().setCropWhite(value);
			}

			@Override
			public int getImageSize() {
				return MainParam.getInstance().getMaxWidth();
			}

			@Override
			public void onImageSize(int width, int height) {
				MainParam.getInstance().setMaxWidth(width);
				MainParam.getInstance().setMaxHeight(height);
			}

			@Override
			public void onRename() {
				batchRenameWork();
			}

			@Override
			public void onAdvancedRename() {
				batchRenamePlusWork();
			}

			@Override
			public void onSplit() {
				batchSplitWork();
			}

			@Override
			public void onCropA() {
				batchCropWork();
			}

			@Override
			public void onCropB() {
				batchCropAndScaleWork();
			}

			@Override
			public void onCropC() {
				batchCropAndScaleByWidthWork();
			}

			@Override
			public void onManual() {
				onMenuManual();
			}

			@Override
			public void onAbout() {
				onMenuAbout();
			}
		};

		editModeMenuBar = new MenuBarInEditMode(cb);
	}

	private void onMenuManual() {
		ManualDlg dlg = new ManualDlg(ComicCrop.this);
		dlg.setVisible(true);
	}

	private void onMenuAbout() {
		JOptionPane.showMessageDialog(null,
				"ComicCrop Version " + StringResources.VERSION + "\n2012-2024 © Woosoft",
				"About",
				JOptionPane.INFORMATION_MESSAGE);
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

	private enum WorkMode {
		ModeView,
		ModeEdit,
	}

	private void switchUI(WorkMode mode) {
		Container container = getContentPane();
		switch (mode) {
			case ModeView:
				setJMenuBar(viewModeMenuBar.getMenuBar());
				container.remove(editor);
				container.remove(mBoard);
				container.add(viewer, BorderLayout.CENTER);
				break;
			case ModeEdit:
				setJMenuBar(editModeMenuBar.getMenuBar());
				container.remove(viewer);
				container.add(editor, BorderLayout.CENTER);
				container.add(mBoard, BorderLayout.EAST);
				break;
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
			switchUI(WorkMode.ModeEdit);

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
			switchUI(WorkMode.ModeView);

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
	
	private void batchCropAndScaleByWidthWork() {
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
	
	static private ComicCrop instance = null;
	
	static public ComicCrop getInstance() {
		return instance;
	}

	public static void main(String[] args) {

		Runnable r = new Runnable() {
			public void run() {
				instance = new ComicCrop();
			}
		};
		EventQueue.invokeLater(r);
	}
}
