package small.indeed.fortunate.Activity;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import small.indeed.fortunate.*;
import small.indeed.fortunate.Browser.*;
import small.indeed.fortunate.Unit.*;
import small.indeed.fortunate.View.*;
import android.preference.*;

public class BrowserActivity extends Activity 
{
	private FrameLayout contentFrame;
	private CustomWebView fastView;
	private ProgressBar progressBar;
	
	private ValueCallback<Uri[]> valueCallback = null;
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            valueCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        }
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		initUi();
		initWebView();
		
		Uri intentData = getIntent().getData();
		String intentAction = getIntent().getAction();
		String intentExtra = getIntent().getStringExtra("query");

		if (Intent.ACTION_VIEW.equals(intentAction)) {
			if (intentData != null) {
				this.fastView.loadUrl(intentData.toString());
			}
		} else if (Intent.ACTION_WEB_SEARCH.equals(intentAction)) {
			if (intentExtra.length() > 0) {
				this.fastView.loadUrl(BrowserUnit.SEARCH_ENGINE_BAIDU + intentExtra);
			} else if (intentExtra.startsWith(BrowserUnit.URL_SCHEME_HTTP) || intentExtra.startsWith(BrowserUnit.URL_SCHEME_HTTPS) || intentExtra.startsWith(BrowserUnit.URL_SCHEME_FILE)) {
				this.fastView.loadUrl(intentExtra);
			}
		} else {
			this.fastView.loadUrl(BrowserUnit.BASE_URL);
		}
	}
	
	@Override
    protected void onDestroy() {
        if (fastView != null) {
            fastView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            fastView.clearHistory();

            ((ViewGroup) fastView.getParent()).removeView(fastView);
			fastView.removeAllViews();
            fastView.destroy();
            fastView = null;
        }
        super.onDestroy();
    }
	
	private void initUi()
	{
		fastView = new CustomWebView(getApplicationContext());
		contentFrame = (FrameLayout) findViewById(R.id.main_content);
		progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);

        contentFrame.addView(fastView, new LinearLayout.LayoutParams(
								 ViewGroup.LayoutParams.MATCH_PARENT,
								 ViewGroup.LayoutParams.MATCH_PARENT));
								 
		}
	
		private void initWebView()
		{
			fastView.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						super.onPageStarted(view, url, favicon);
						if (url.contains("baidu.com/wap/link") || url.contains("baidu.com/wap/shareview") || url.contains("baidu.com/s/") || url.contains("baidu.com/wap/view")) {
							fastView.getSettings().setUserAgentString(BrowserUnit.UA_SYMBIAN);
						} else {
							fastView.getSettings().setUserAgentString(fastView.getSettings().getUserAgentString());
						}
//						if ((Uri.parse(url).getAuthority() + Uri.parse(url).getLastPathSegment()).startsWith("pan.baidu.comlink")) {
//							fastView.getSettings().setUserAgentString(BrowserUnit.UA_SYMBIAN);
//						} else {
//							fastView.getSettings().setUserAgentString(fastView.getSettings().getUserAgentString());
//						}
						SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BrowserActivity.this);
						fastView.getSettings().setBlockNetworkImage(!sp.getBoolean(getString(R.string.sp_images), true));
						
					}
					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
					}
					@Override
					public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
						super.onReceivedError(view, errorCode, description, failingUrl);
						ToastUtil.show(BrowserActivity.this, description);
					}
					@Override
					public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
						super.doUpdateVisitedHistory(view, url, isReload);
					}
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, final String url) {
						if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith(WebView.SCHEME_GEO) || url.startsWith(WebView.SCHEME_MAILTO)) {
							try {
								Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
								startActivity(intent);
								return true;
							} catch (Exception e) {
								ToastUtil.show(BrowserActivity.this, R.string.toast_intent_failed);
							}
						}
						
						return super.shouldOverrideUrlLoading(view, url);
					}
					@Deprecated
					@Override
					public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
						return super.shouldInterceptRequest(view, url);
					}
					@Override
					public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
						return super.shouldInterceptRequest(view, request);
					}
					@Override
					public void onFormResubmission(WebView view, final Message dontResend, final Message resend) {
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
						builder.setCancelable(false);
						builder.setTitle(R.string.dialog_title_resubmission);
						builder.setMessage(R.string.dialog_content_resubmission);
						builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									resend.sendToTarget();
								}
							});
						builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dontResend.sendToTarget();
								}
							});
						builder.create().show();
					}
					@Override
					public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
						builder.setCancelable(false);
						builder.setTitle(R.string.dialog_title_warning);
						builder.setMessage(R.string.dialog_content_ssl_error);
						builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									handler.proceed();
								}
							});
						builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									handler.cancel();
								}
							});
						AlertDialog dialog = builder.create();
						if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
							dialog.show();
						} else {
							handler.proceed();
						}
					}
					@Override
					public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
						builder.setCancelable(false);
						builder.setTitle(R.string.dialog_title_sign_in);
						LinearLayout signInLayout = (LinearLayout) LayoutInflater.from(BrowserActivity.this).inflate(R.layout.dialog_sign_in, null, false);
						final EditText userEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_username);
						final EditText passEdit = (EditText) signInLayout.findViewById(R.id.dialog_sign_in_password);
						passEdit.setTypeface(Typeface.DEFAULT);
						passEdit.setTransformationMethod(new PasswordTransformationMethod());
						builder.setView(signInLayout);
						builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String user = userEdit.getText().toString().trim();
									String pass = passEdit.getText().toString().trim();
									handler.proceed(user, pass);
								}
							});
						builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									handler.cancel();
								}
							});
						builder.create().show();
					}
				});
				
			fastView.setWebChromeClient(new WebChromeClient() {
					@Override
					public void onProgressChanged(WebView view, int progress) {
						super.onProgressChanged(view, progress);
						if (progress < 2) {
							progressBar.setProgress(2);
						} else if (progress > progressBar.getProgress()) {
							ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", progress);
							//animator.setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime));
							animator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
							animator.setInterpolator(new DecelerateInterpolator());
							animator.start();
						} else {
							progressBar.setProgress(progress);
						}
						if (progress == 100) {
							ObjectAnimator outAni = ObjectAnimator.ofFloat(progressBar, "alpha", 1, 0);
							outAni.setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime));
							outAni.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
							outAni.addListener(new Animator.AnimatorListener() {
									@Override
									public void onAnimationStart(Animator animation) {}
									@Override
									public void onAnimationEnd(Animator animation) {
										progressBar.setVisibility(View.GONE);
										progressBar.setAlpha(1);
										
									}
									@Override
									public void onAnimationCancel(Animator animation) {}
									@Override
									public void onAnimationRepeat(Animator animation) {}
								});
							outAni.start();
						} else {
							progressBar.setVisibility(View.VISIBLE);
							
						}
					}
					@Override
					public void onReceivedTitle(WebView view, String title) {
						super.onReceivedTitle(view, title);
					}
					@Override
					public void onReceivedIcon(WebView view, Bitmap icon) {
						super.onReceivedIcon(view, icon);
					}//imageView.setImageBitmap(icon);
					@Deprecated
					@Override
					public void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
						super.onShowCustomView(view, requestedOrientation, callback);
						onShowCustomView(view, requestedOrientation, callback);
					}
					@Override
					public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
						super.onShowCustomView(view, callback);
						if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
						}
					}
					@Override
					public void onHideCustomView() {
						super.onHideCustomView();
						if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						}
					}
					@Override
					public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
						super.onGeolocationPermissionsShowPrompt(origin, callback);
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
						builder.setMessage("允许网页访问地理位置");
						builder.setCancelable(false);
						builder.setPositiveButton("允许", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									callback.invoke(origin, true, true);
								}
							});
						builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									callback.invoke(origin, false, true);
								}
							});
						builder.show();
					}
					//设置响应js 的Alert()函数
					@Override
					public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
						AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
						b.setMessage(message);
						b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									result.confirm();
								}
							});
						b.setCancelable(false);
						b.create().show();
						return true;
					}
					//设置响应js 的Confirm()函数
					@Override
					public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
						AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
						b.setMessage(message);
						b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									result.confirm();
								}
							});
						b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									result.cancel();
								}
							});
						b.setCancelable(false);
						b.create().show();
						return true;
					}
					//设置响应js 的Prompt()函数
					@Override
					public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
						View v =  LayoutInflater.from(BrowserActivity.this).inflate(R.layout.dialog_edit, null);
						final EditText editText = (EditText) v.findViewById(R.id.layout_dialog_edit_text);
						editText.setText(defaultValue);
						AlertDialog.Builder b = new AlertDialog.Builder(BrowserActivity.this);
						b.setMessage(message);
						b.setView(v);
						b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String value = editText.getText().toString().trim();
									result.confirm(value);
								}
							});
						b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									result.cancel();
								}
							});
						b.setCancelable(false);
						b.create().show();
						return true;
					}
					// For Android 5.0+
					@Override
					public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							valueCallback = filePathCallback;
							try {
								Intent intent = fileChooserParams.createIntent();
								startActivityForResult(intent, IntentUnit.REQUEST_FILE_21);
							} catch (Exception e) {
								ToastUtil.show(BrowserActivity.this, R.string.toast_open_file_manager_failed);
							}
						}
						return true;
					}
				});
				
			fastView.setDownloadListener(new DownloadListener() {
					@Override
					public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
						ArrayList<String> list = new ArrayList<String>();
						list.add(0, "调用系统下载");
						if (BrowserUnit.hasApp(BrowserActivity.this, "com.dv.adm.pay")) {
							list.add(1, "调用ADM下载");
						} else {
							list.add(1, "调用ADM下载(需要下载)");
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
						builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
										case 0:
											BrowserUnit.download(BrowserActivity.this, url, contentDisposition, mimeType);
											break;
										case 1:
											BrowserUnit.downloadByADM(BrowserActivity.this, url, mimeType);
											break;
										default:
											break;
									}
								}
							});
						builder.show();
					}
				});

			fastView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						final WebView.HitTestResult result = fastView.getHitTestResult();
						final String target = result.getExtra();
						
						final ArrayList<String> list = new ArrayList<String>();
						list.add(getString(R.string.dialog_longpress_copy_link));
						if (result != null && (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
							list.add(getString(R.string.dialog_longpress_save_img));
						}
						list.add(getString(R.string.dialog_longpress_page_details));
						list.add(getString(R.string.dialog_longpress_new_tab));
						
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
						builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String s = list.get(which);
									if (s.equals(getString(R.string.dialog_longpress_copy_link))) {
										BrowserUnit.copyURL(BrowserActivity.this, target);
									} else if (s.equals(getString(R.string.dialog_longpress_save_img))) {
											BrowserUnit.downloadByADM(BrowserActivity.this, target, BrowserUnit.MIME_TYPE_IMAGE);
									} else if (s.equals(getString(R.string.dialog_title_page_details))) {
										AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
										builder.setTitle(getString(R.string.dialog_longpress_page_details));
										builder.setMessage(
											getString(R.string.dialog_content_page_title) + "\n" + fastView.getTitle() + "\n" + "\n" +
											getString(R.string.dialog_content_page_link) + "\n" + fastView.getUrl() + "\n" + "\n" +
											getString(R.string.dialog_content_page_select_link) + "\n" + target
										);
										builder.setPositiveButton(android.R.string.ok, null);
										AlertDialog alertDialog = builder.create();
										alertDialog.getWindow().setWindowAnimations(R.style.dialogAnimation);
										alertDialog.show();
									} else if (s.equals(getString(R.string.dialog_longpress_new_tab))) {
									}
									
								}
							});
						
						if (target != null || (result != null && result.getExtra() != null)) {
							if (result == null) {
								result.getExtra();
							}
							AlertDialog alertDialog = builder.create();
							alertDialog.getWindow().setWindowAnimations(R.style.dialogAnimation);
							alertDialog.show();
						}
						
						return false;
					}
				});
				
		}
		
		private void showBookmark() {
			LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_bookmark, null, false);
			
			TextView textView = (TextView) layout.findViewById(R.id.dialog_bookmark_empty);
			ListView listView = (ListView) layout.findViewById(R.id.dialog_bookmark_list);
			List<Map<String, Object>> data;
			int number = 0;
			String bookmarkTitle;
			String bookmarkUrl;


			if (number == 0) {
				listView.setVisibility(View.GONE);
				textView.setVisibility(View.VISIBLE);
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
			builder.setView(layout);
			AlertDialog alertDialog = builder.create();
			alertDialog.getWindow().setWindowAnimations(R.style.dialogAnimation);
			alertDialog.show();
		}
		
	private boolean showOverflow() {
		ArrayList<String> list = new ArrayList<String>();
		if (fastView.getProgress() == 100) {
			list.add(0, "刷新网页");
		} else {
			list.add(0, "停止刷新");
		}
		list.add(1, "下载管理");
		list.add(2, "分享");
		list.add(3, "发至桌面");
		if (fastView.getSettings().getUserAgentString().equals(BrowserUnit.UA_DESKTOP)) {
			list.add(4, "切换手机版网页");
		} else {
			list.add(4, "切换电脑版网页");
		}
		list.add(5, "浏览设置");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
		builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
						case 0:
							if (fastView.getProgress() == 100) {
								fastView.reload();
							} else {
								fastView.stopLoading();
							}
							break;
						case 1:
							//通过包名启动应用
							try { 
								Intent intent = getPackageManager().getLaunchIntentForPackage("com.dv.adm.pay");
								startActivity(intent); 
							} catch (Exception e) { 
								Intent i = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
								startActivity(i); 
							}
							break;
						case 2:
							String url = fastView.getUrl();
							if (url != null) {
								try {
									IntentUnit.share(BrowserActivity.this, url);
								} catch (Exception e) {
									ToastUtil.show(BrowserActivity.this, R.string.toast_failed);
								}
							}
							break;
						case 3:
							try {
								Intent shortcutIntent = new Intent(BrowserActivity.this, BrowserActivity.class);
								shortcutIntent.setData(Uri.parse(fastView.getUrl()));
								shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载

								Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
								addIntent.putExtra("duplicate", false);	// 不重复创建快捷方式图标
								addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
								addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, fastView.getTitle());
								addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, fastView.getFavicon());
								sendBroadcast(addIntent);
								ToastUtil.show(BrowserActivity.this, R.string.toast_successful);
							} catch (Exception e) {
								ToastUtil.show(BrowserActivity.this, R.string.toast_failed);
							}
							break;
						case 4:
							if (fastView.getSettings().getUserAgentString().equals(BrowserUnit.UA_DESKTOP)) {
								fastView.getSettings().setUserAgentString(null);
								fastView.reload();
							} else {
								fastView.getSettings().setUserAgentString(BrowserUnit.UA_DESKTOP);
								fastView.reload();
							}
							break;
						case 5:
							showBookmark();
							//Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + getPackageName()));
							//i.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.InstalledAppDetails"));
							//startActivity(i);
							//Intent intent = new Intent(BrowserActivity.this, SettingActivity.class);
							//startActivity(intent);
							break;
						default:
							break;
					}
				}
			});
		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().setWindowAnimations(R.style.dialogAnimation);
		alertDialog.getWindow().setGravity(Gravity.CENTER | Gravity.BOTTOM);
		alertDialog.show();
		
		return true;
	}
		
	private boolean onKeyCodeVolumeUp() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int vc = Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"));

        if (vc == 0) { // Switch tabs
            return true;
        } else if (vc == 1) { // Scroll webpage
			if (fastView.getScrollY() != 0) {
				//	view.setScrollY(0);   //滚动到顶部
				int height = fastView.getMeasuredHeight();
				int scrollY = fastView.getScrollY();
				int distance = Math.min(height, scrollY);

				ObjectAnimator anim = ObjectAnimator.ofInt(fastView, "scrollY", scrollY, scrollY - distance);
				anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
				anim.start();
			}
			
            return true;
        }

        return false;
    }

    private boolean onKeyCodeVolumeDown() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int vc = Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"));

        if (vc == 0) { // Switch tabs
            return true;
        } else if (vc == 1) {
			if (fastView.getContentHeight() * fastView.getScale() - (fastView.getHeight() + fastView.getScrollY()) != 0) {
				int height = fastView.getMeasuredHeight();
				int scrollY = fastView.getScrollY();
				int surplus = (int) (fastView.getContentHeight() * fastView.getScale() - (fastView.getHeight() + fastView.getScrollY()));
				int distance = Math.min(height, surplus);

				ObjectAnimator anim = ObjectAnimator.ofInt(fastView, "scrollY", scrollY, scrollY + distance);
				anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
				anim.start();
			}
			
            return true;
        }

        return false;
    }
		
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // When video fullscreen, just control the sound
            return onKeyCodeVolumeUp();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // When video fullscreen, just control the sound
            return onKeyCodeVolumeDown();
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            return showOverflow();
		}

        return super.onKeyDown(keyCode, event);
    }
		
//	@Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//            int vc = Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"));
//            if (vc != 2) {
//                return true;
//            }
//        }
//
//        return false;
//    }
		
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this).setMessage("清楚浏览数据并退出.").setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						fastView.clearHistory();
						fastView.clearFormData();
						fastView.clearCache(true);
						finish();
					}
				}).show();
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (fastView.canGoBack()) {
			fastView.goBack();
		} else {
			finish();
		}
	}
	
}
