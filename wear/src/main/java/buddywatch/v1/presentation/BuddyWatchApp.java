package buddywatch.v1.presentation;

import android.app.Application;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

public class BuddyWatchApp extends Application implements ViewModelStoreOwner {
    private final ViewModelStore modelStore = new ViewModelStore();

    @Override
    public ViewModelStore getViewModelStore(){
        return modelStore;
    }

}