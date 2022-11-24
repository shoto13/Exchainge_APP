package com.journey13.exchainge;

import androidx.recyclerview.widget.DiffUtil;

import com.journey13.exchainge.Model.Chat;

import java.util.List;

public class RecyclerDiffUtilCallback extends DiffUtil.Callback {

    private List<Chat> oldList;
    private List<Chat> newList;

    public RecyclerDiffUtilCallback(List<Chat> oldList, List<Chat> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldPosition, int newPosition) {
        return oldPosition == newPosition;
    }

    @Override
    public boolean areContentsTheSame(int oldPosition, int newPosition) {
        return oldList.get(oldPosition) == newList.get(newPosition);
    }

    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
