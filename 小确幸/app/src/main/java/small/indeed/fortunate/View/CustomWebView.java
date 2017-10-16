package small.indeed.fortunate.View;

import android.content.*;
import android.os.*;
import android.webkit.*;
import small.indeed.fortunate.Unit.*;

public class CustomWebView extends WebView
{
	private Context context;

    public CustomWebView(Context context) {
        super(context); // Cannot create a dialog, the WebView context is not an Activity
		this.context = context;

        initWebSettings();
	}

	private synchronized void initWebSettings()
	{
		WebSettings webSettings = getSettings();
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);

		webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(context.getCacheDir().toString());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
		webSettings.setGeolocationEnabled(true);
        webSettings.setGeolocationDatabasePath(context.getFilesDir().toString());

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webSettings.setTextZoom(100);
        webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);

		webSettings.setSaveFormData(true);
		webSettings.setSavePassword(true);
        //webSettings.setBlockNetworkImage(!true);
		webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(false);
		webSettings.setPluginState(WebSettings.PluginState.ON);
		webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		webSettings.setDefaultTextEncodingName(BrowserUnit.URL_ENCODING_UTF);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			WebView.enableSlowWholeDocumentDraw();
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

	}

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl,
                                   final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

	public synchronized void pause() {
        onPause();
        pauseTimers();
    }

    public synchronized void resume() {
        onResume();
        resumeTimers();
    }

    @Override
    public synchronized void destroy() {
        stopLoading();
        onPause();
        clearHistory();
        setVisibility(GONE);
        removeAllViews();
        destroyDrawingCache();
        super.destroy();
    }
}


