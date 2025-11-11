package buddywatch.v1.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TutViewModel extends ViewModel {

    private final MutableLiveData<String> tut = new MutableLiveData<>();

    public LiveData<String> getFilePath(){
        return tut;
    }

    public void setFilePath(String path){
        tut.postValue(path);
    }

}