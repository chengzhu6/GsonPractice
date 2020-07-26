package com.thoughtworks.roompractice.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thoughtworks.roompractice.R;
import com.thoughtworks.roompractice.common.MyApplication;
import com.thoughtworks.roompractice.common.RxManager;
import com.thoughtworks.roompractice.entity.Person;

import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.thoughtworks.roompractice.common.ToastUtil.showToast;

public class SubmitActivity extends AppCompatActivity {

    private static final List<Integer> GENDERS = Arrays.asList(0, 1);
    private EditText nameEditText;
    private EditText ageEditText;
    private EditText genderEditText;
    private Button submitButton;
    private RxManager rxManager = new RxManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        nameEditText = findViewById(R.id.name);
        ageEditText = findViewById(R.id.age);
        genderEditText = findViewById(R.id.gender);
        submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(view -> {
            Person person = getInputData();
            if (checkInput(person)) {
                submitData(person);
            } else {
                showToast("Input Invalid");
            }
        });
    }

    private void submitData(Person person) {
        Disposable disposable = MyApplication.getLocalDataSource().personDao().insertPerson(person)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    rxManager.add(disposable1);
                    submitButton.setClickable(false);
                })
                .doFinally(() -> submitButton.setClickable(true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> showToast("Insert Success"));
        rxManager.add(disposable);
    }

    private boolean checkInput(Person person) {
        return person.age > 0 && GENDERS.contains(person.gender);
    }

    @Override
    protected void onDestroy() {
        rxManager.dispose();
        super.onDestroy();
    }

    private Person getInputData() {
        Person person = new Person();
        person.name = nameEditText.getText().toString();
        person.age = Integer.parseInt(ageEditText.getText().toString());
        person.gender = Integer.parseInt(genderEditText.getText().toString());
        return person;
    }

}