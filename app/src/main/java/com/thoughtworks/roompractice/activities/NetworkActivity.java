package com.thoughtworks.roompractice.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.thoughtworks.roompractice.R;
import com.thoughtworks.roompractice.common.RxManager;
import com.thoughtworks.roompractice.constracts.Person;
import com.thoughtworks.roompractice.constracts.PersonWrapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.thoughtworks.roompractice.common.ToastUtil.showToast;

public class NetworkActivity extends AppCompatActivity {
    private static final String URL = "https://twc-android-bootcamp.github.io/fake-data/data/default.json";
    private Button requestButton;
    private RxManager rxManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        rxManager = new RxManager();
        requestButton = findViewById(R.id.request);
        requestButton.setOnClickListener(view -> request());
    }

    @Override
    protected void onDestroy() {
        rxManager.dispose();
        super.onDestroy();
    }

    private void request() {
        Observable.create(createObservable())
                .map(s -> {
                    final Gson gson = new Gson();
                    return gson.fromJson(s, PersonWrapper.class);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createObserve());
    }

    @NotNull
    private Observer<PersonWrapper> createObserve() {
        return new Observer<PersonWrapper>() {
            @Override
            public void onSubscribe(Disposable d) {
                requestButton.setClickable(false);
                rxManager.add(d);
            }

            @Override
            public void onNext(PersonWrapper personWrapper) {
                List<Person> data = personWrapper.getData();
                if (data != null && !data.isEmpty()) {
                    Person person = data.get(0);
                    if (person != null) {
                        showToast(person.getName());
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                showToast(e.getMessage());
            }

            @Override
            public void onComplete() {
                requestButton.setClickable(true);
            }
        };
    }

    @NotNull
    private ObservableOnSubscribe<String> createObservable() {
        return emitter -> {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request.Builder requestBuilder = new Request.Builder();
                Request request = requestBuilder.url(URL).build();
                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            emitter.onNext(body.string());
                            emitter.onComplete();
                        }
                    } else {
                        emitter.onError(new Exception("Request Failure"));
                    }
                } catch (IOException e) {
                    emitter.onError(e);
                }
            };
    }

}