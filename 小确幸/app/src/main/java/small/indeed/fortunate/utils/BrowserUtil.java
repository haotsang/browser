package small.indeed.fortunate.utils;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.Bitmap.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.webkit.*;
import java.util.*;
import java.util.regex.*;
import small.indeed.fortunate.*;
import java.net.*;
import java.io.*;
import android.content.res.*;
import android.support.annotation.*;
import android.widget.*;

public class BrowserUtil {
	
	public static final String SUFFIX_HTML = ".html";
    public static final String SUFFIX_PNG = ".png";
    public static final String SUFFIX_TXT = ".txt";

	public static final String MIME_TYPE_TEXT_HTML = "text/html";
    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_IMAGE = "image/*";

	public static final String BASE_URL = "file:///android_asset/index.html";
	public static final String HOME_PAGE = "http://m.ilxdh.com/";
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
	
	public static final String INTRODUCTION_EN = "ninja_introduction_en.html";
    public static final String INTRODUCTION_ZH = "ninja_introduction_zh.html";
	
	public static final String URL = "url";
    public static final String FOLDER = "folder";
	
	/**
     * 实现textView的Compound图标变色
     *
     * @param textView
     * @param color
     * @param isChangeTextColor 是否改变字的颜色
     */
    public static void setCompoundTint(@NonNull TextView textView, @ColorInt int color, boolean isChangeTextColor) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (isChangeTextColor) {
            textView.setTextColor(s1);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setCompoundDrawableTintList(s1);
        } else {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                mode = PorterDuff.Mode.MULTIPLY;
            }
            for (Drawable d : textView.getCompoundDrawables()) {
                if (d != null)
                    d.setColorFilter(color, mode);
            }

        }
    }
	
	public static String ClipboardUrl(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService("clipboard");
        String str = "";
        if (clipboardManager != null) {
            try {
                if (clipboardManager.hasText()) {
                    CharSequence text = clipboardManager.getText();
                    clipboardManager.setText(text);
                    if (text != null && text.length() > 0) {
                        return text.toString().trim();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return str;
    }
	
	
	public static void setStatusBarColor(Activity activity, WebView view) {
		if(Build.VERSION.SDK_INT >= 21) {
			Bitmap var1 = BrowserUtil.getBitmapFromView(view);
			if(var1 != null) {
				int var2 = var1.getPixel(activity.getWindowManager().getDefaultDisplay().getWidth() / 2, 10);
				int var3 = Color.red(var2);
				int var4 = Color.green(var2);
				int var5 = Color.blue(var2);
				if(Build.VERSION.SDK_INT >= 23) {
					if(var3 > 200 && var4 > 200 && var5 > 200) {
						activity.getWindow().getDecorView().setSystemUiVisibility(8192);
					} else {
						activity.getWindow().getDecorView().setSystemUiVisibility(0);
					}
				}
				BrowserUtil.setWindowStatusBarColor(activity, var2);
				var1.recycle();
			}
		}
	}
	
	public static void setWindowStatusBarColor(Activity activity, int var1) {
		try {
			if(Build.VERSION.SDK_INT >= 21) {
				Window var3 = activity.getWindow();
				var3.addFlags(Integer.MIN_VALUE);
				var3.setStatusBarColor(var1);
			}
		} catch (Exception var4) {
			var4.printStackTrace();
		}
	}
	
	//状态栏变色
	public static Bitmap getBitmapFromView(View var1) {
		Bitmap var2 = Bitmap.createBitmap(var1.getWidth(), var1.getHeight(), Config.RGB_565);
		Canvas var3 = new Canvas(var2);
		var1.layout(var1.getLeft(), var1.getTop(), var1.getRight(), var1.getBottom());
		Drawable var4 = var1.getBackground();
		if(var4 != null) {
			var4.draw(var3);
		} else {
			var3.drawColor(-1);
		}
		var1.draw(var3);
		return var2;
	}
	
	public static String HtmlText(String var0) {
		String var1 = var0.replaceAll("<title>[^>]*</title>", "");

		try {
			String var3 = Pattern.compile("<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>", 2).matcher(var1).replaceAll("");
			String var4 = Pattern.compile("<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>", 2).matcher(var3).replaceAll("");
			String var5 = Pattern.compile("<[^>]+>", 2).matcher(var4).replaceAll("");
			return var5;
		} catch (Exception var6) {
			return var1;
		}
	}
	
    public static boolean isURL(String url) {
        if (url == null) {
            return false;
        }

        url = url.toLowerCase(Locale.getDefault());
        if (url.startsWith(URL_ABOUT_BLANK)
			|| url.startsWith(URL_SCHEME_MAIL_TO)
			|| url.startsWith(URL_SCHEME_FILE)) {
            return true;
        }

        String regex = "^((ftp|http|https|intent)?://)"                      // support scheme
			+ "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
			+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}"                            // IP形式的URL -> 199.194.52.184
			+ "|"                                                        // 允许IP和DOMAIN（域名）
			+ "([0-9a-z_!~*'()-]+\\.)*"                                  // 域名 -> www.
			+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\."                    // 二级域名
			+ "[a-z]{2,6})"                                              // first level domain -> .com or .museum
			+ "(:[0-9]{1,4})?"                                           // 端口 -> :80
			+ "((/?)|"                                                   // a slash isn't required if there is no file name
			+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).matches();
    }
	
	public static void hideSoftInput(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        ((InputMethodManager) activity.getSystemService("input_method")).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    // toogle输入法  
    public static void hidenOrShowInputMethod(View view,Context context) {  

        InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext()  
			.getSystemService(Context.INPUT_METHOD_SERVICE);  
        // 进行取反  
        inputMethodManager.toggleSoftInputFromWindow(view.getWindowToken(),  
													 InputMethodManager.SHOW_FORCED,  
													 InputMethodManager.HIDE_NOT_ALWAYS);  
    }  
    // 显示输入法  
    public static void showInputMethod(View view,Context context) {  
        InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext()  
			.getSystemService(Context.INPUT_METHOD_SERVICE);  
        //同时再使用该方法之前，view需要获得焦点，可以通过requestFocus()方法来设定。  
        view.requestFocus();  
        inputMethodManager.showSoftInput(view, inputMethodManager.SHOW_FORCED);  
    }  
    //隐藏输入法  
    public static void hidenInputMethod(View view,Context context) {  
        InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext()  
			.getSystemService(Context.INPUT_METHOD_SERVICE);  
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),  
												   InputMethodManager.HIDE_NOT_ALWAYS);  
    }  
    //判断输入法是否已经打开  
    public static boolean isInputMethodOpened(Context context){  
        InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext()  
			.getSystemService(Context.INPUT_METHOD_SERVICE);  
        return inputMethodManager.isActive();  
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

	/**
     * 判断相对应的APP是否存在
     *
     * @param context
     * @param packageName
     * @return
     */
    public boolean isAvilible(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName
				.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
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



