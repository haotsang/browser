package small.indeed.fortunate.Fragment;

import android.os.Bundle;  
import android.support.annotation.Nullable;  
import android.support.v4.app.Fragment;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;
import small.indeed.fortunate.*;  

public class MarkHistoryFragment extends Fragment {  
	@Nullable  
	@Override  
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {  
		View view = inflater.inflate(R.layout.activity_mark_history, null);  
		return view;  
	} 
}
