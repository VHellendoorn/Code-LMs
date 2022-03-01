package net.contentobjects.jnotify.linux;

import net.contentobjects.jnotify.JNotifyException;

public class JNotifyException_linux extends JNotifyException
{
	private static final long serialVersionUID = 1L;

	private static final int LINUX_NO_SUCH_FILE_OR_DIRECTORY = 2;
	private static final int LINUX_PERMISSION_DENIED = 13;
	private static final int LINUX_NO_SPACE_LEFT_ON_DEVICE = 28;

	public JNotifyException_linux(String s, int systemErrorCode)
	{
		super(s, systemErrorCode);
	}

	public int getErrorCode()
	{
		switch (_systemErrorCode)
		{
		case LINUX_PERMISSION_DENIED:
			return ERROR_PERMISSION_DENIED;
		case LINUX_NO_SPACE_LEFT_ON_DEVICE:
			return ERROR_WATCH_LIMIT_REACHED;
		case LINUX_NO_SUCH_FILE_OR_DIRECTORY:
			return ERROR_NO_SUCH_FILE_OR_DIRECTORY;
		default:
			return ERROR_UNSPECIFIED;
		}
	}
}
