package com.aar.app.todomemory.edittodo;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.aar.app.todomemory.R;
import com.aar.app.todomemory.model.ToDo;

public class ToDoEditorFragment extends Fragment {

    private EditText mToDo;
    private CheckBox mImportant;
    private CheckBox mIsDone;
    private ToDoEditorViewModel mViewModel;
    private OnSaveSuccessfulListener mOnSaveSuccessfullyListener;

    public interface OnSaveSuccessfulListener {
        void onSaveSuccess();
    }

    public static ToDoEditorFragment newInstance(long todoId) {
        Bundle args = new Bundle();
        args.putLong("KEY_TODO_ID", todoId);
        ToDoEditorFragment fragment = new ToDoEditorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ToDoEditorFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textMessage = view.findViewById(R.id.textMessage);
        View buttonSave = view.findViewById(R.id.buttonSave);
        View buttonClear = view.findViewById(R.id.buttonClear);
        mIsDone = view.findViewById(R.id.checkBoxDone);
        mImportant = view.findViewById(R.id.checkImportant);
        mToDo = view.findViewById(R.id.editTextTodo);
        showKeyboardOnToDo();

        mViewModel = ViewModelProviders.of(this).get(ToDoEditorViewModel.class);
        mViewModel.getToDoLiveData().observe(this, toDo -> {
            if (toDo != null) {
                mToDo.setText(toDo.getContent());
                mToDo.setSelection(mToDo.getText().length());
                mImportant.setChecked(toDo.isImportant());
                if (toDo.getState() == ToDo.STATE_DONE) {
                    mIsDone.setVisibility(View.VISIBLE);
                    mIsDone.setChecked(true);
                } else {
                    mIsDone.setVisibility(View.GONE);
                    mIsDone.setChecked(false);
                }
            }
        });
        mViewModel.getStateLiveData().observe(this, state -> {
            if (state != null) {
                switch (state) {
                    case NEW_EMPTY_TODO:
                        textMessage.setText("New to-do");
                        break;
                    case EDIT_DRAFT:
                        textMessage.setText("Edit draft");
                        break;
                    case EDIT_TODO:
                        textMessage.setText("Edit to-do");
                        break;
                    case EDIT_DONE:
                        textMessage.setText("Saved");
                        hideKeyboard();
                        if (mOnSaveSuccessfullyListener != null) {
                            mOnSaveSuccessfullyListener.onSaveSuccess();
                        }
                        break;
                }
            }
        });

        long id = getToDoId();
        if (id <= 0) {
            mViewModel.newToDo();
        } else {
            mViewModel.editToDo(id);
        }

        buttonSave.setOnClickListener(v ->
                mViewModel.saveToDo(mToDo.getText().toString(), mImportant.isChecked(), mIsDone.isChecked()));
        buttonClear.setOnClickListener(v -> mViewModel.clear());
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.saveDraft(mToDo.getText().toString(), mImportant.isChecked());
    }

    public void setOnSaveSuccessfullyListener(OnSaveSuccessfulListener listener) {
        mOnSaveSuccessfullyListener = listener;
    }

    private void showKeyboardOnToDo() {
        mToDo.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mToDo, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mToDo.getWindowToken(), 0);
    }

    private long getToDoId() {
        if (getArguments() == null) return 0;
        return getArguments().getLong("KEY_TODO_ID");
    }
}