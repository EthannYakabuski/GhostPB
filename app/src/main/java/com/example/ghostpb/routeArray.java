
package com.example.ghostpb;

public class routeArray {
    public String[] s;
    public int capacity;
    public int size;

    public routeArray(int capacity) {
        this.capacity = capacity;
        s = new String[capacity];
    }

    //return T/F depending if array is full
    private boolean isFull() {return capacity == size;}

    private int getSize() {return size;}

    //double capacity of the array
    private void addOneSize() {
        capacity = capacity+1;
        String[] copy = new String[capacity];
        for (int i = 0; i < size; i++) {
            copy[i] = s[i];
        }
        s = copy;
    }

    //Add new element to end
    public void push(String element) {
        if (isFull()) {
            addOneSize();
        }
        s[size] = element;
        size++;
    }

    /*
    public void remove(String element) {
        int newSize;
        for (int i = 0; i < size; i++) {
            if(s[i] != element) {
                s[newSize++] = s[i];
            }
        }
        size = newSize;
    }
    */
}




