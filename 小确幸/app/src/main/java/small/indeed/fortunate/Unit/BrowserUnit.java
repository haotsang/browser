package small.indeed.fortunate.Unit;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.webkit.*;
import java.util.regex.*;
import small.indeed.fortunate.*;
import small.indeed.fortunate.View.*;

public class BrowserUnit {
	
	public static final String SUFFIX_HTML = ".html";
    public static final String SUFFIX_PNG = ".png";
    public static final String SUFFIX_TXT = ".txt";

	public static final String MIME_TYPE_TEXT_HTML = "text/html";
    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_IMAGE = "image/*";

	public static final String BASE_URL = "file:///android_asset/homepage.html";
    public static final String BOOKMARK_TYPE = "<DT><A HREF=\"{url}\" ADD_DATE=\"{time}\">{title}</A>";
    public static final String BOOKMARK_TITLE = "{title}";
    public static final String BOOKMARK_URL = "{url}";
    public static final String BOOKMARK_TIME = "{time}";

	public static final String SEARCH_ENGINE_BAIDU = "https://m.baidu.com/s?from=&wd=";
    public static final String SEARCH_ENGINE_BING = "https://cn.bing.com/search?q=";
	public static final String SEARCH_ENGINE_GOOGLE = "https://www.google.com/search?q=";

	public static final String UA_SYMBIAN = "Mozilla/5.0 (Symbian/3; Series60/5.2 NokiaN8-00/012.002; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/533.4 (KHTML, like Gecko) NokiaBrowser/7.3.0 Mobile Safari/533.4 3gpp-gba";
	public static final String UA_DESKTOP = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

	public static final String URL_ENCODING_UTF = "UTF-8";
	public static final String URL_ENCODING_GBK = "GBK";

	public static final String URL_ABOUT_BLANK = "about:blank";
    public static final String URL_SCHEME_ABOUT = "about:";
    public static final String URL_SCHEME_MAIL_TO = "mailto:";
	public static final String URL_SCHEME_FOLDER = "folder://";
    public static final String URL_SCHEME_FILE = "file://";
    public static final String URL_SCHEME_FTP = "ftp://";
    public static final String URL_SCHEME_HTTP = "http://";
    public static final String URL_SCHEME_HTTPS = "https://";
    public static final String URL_SCHEME_INTENT = "intent://";

	public static boolean isUrl(String trim) {
        String pattern = "^(((file|gopher|news|nntp|telnet|http|ftp|https|ftps|sftp)://)|(www\\.))+(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(trim);
        return m.matches();
	}

	public static void showSoftInput(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

	public static void hideSoftInput(Context context, View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

	public static void copyURL(Context context, String url) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText(null, url.trim());
        manager.setPrimaryClip(data);
        ToastUtil.show(context, R.string.toast_copy_link_successfully);
    }

    public static void download(Context context, String url, String contentDisposition, String mimeType) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String filename = URLUtil.guessFileName(url, contentDisposition, mimeType); // Maybe unexpected filename.

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(filename);
        request.setMimeType(mimeType);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        ToastUtil.show(context, R.string.toast_start_download);
    }

	public static void downloadByADM(Context context, String url, String mimeType) {
        if (hasApp(context, "com.dv.adm.pay")) {
            try {
				Intent startApp = new Intent(Intent.ACTION_VIEW);
				startApp.setComponent(new ComponentName("com.dv.adm.pay", "com.dv.adm.pay.AEditor"));
				startApp.setDataAndType(Uri.parse(url), mimeType);
				context.startActivity(startApp);
            } catch (Exception e) {
            }
        }
    }

	public static boolean hasApp(Context context, String packgename) {
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(packgename, 0);
		} catch (Exception e) {
			packageInfo = null;
			e.printStackTrace();
		} if (packageInfo == null) {
			return false;
		} else {
			return true;
		}
	}


}



