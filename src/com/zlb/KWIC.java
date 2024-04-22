package com.zlb;

/**
 * ClassName: spa_1
 * Package:　com.zlb
 * Create: 2024/4/19 14:45
 */
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class KWIC{



    private char[] chars_;



    private int[] line_index_;



    private int[][] circular_shifts_;


    private int[][] alphabetized_;

    public void input(String file){

        // initialize line index
        line_index_ = new int[32];

        // initialize chars array
        chars_ = new char[2048];

        // count of valid characters in the buffer
        int char_count = 0;

        // count of parsed lines
        int line_count = 0;

        // the last read character
        int c;

        // new line flag
        boolean is_new_line = true;

        // new word flag
        boolean is_new_word = false;

        // line started flag
        boolean is_line_started = false;

        try{

            // open the file for reading
            InputStream in = new FileInputStream(file);

            // read characters until EOF is reached
            c = in.read();
            while(c != -1){

                // parse the character
                switch((byte) c){
                    case '\n':
                        is_new_line = true;
                        break;
                    case ' ':
                        is_new_word = true;
                        break;
                    case '\t':
                        is_new_word = true;
                        break;
                    case '\r':
                        break;
                    default:

                        // if this is a new line we need to update the line index
                        if(is_new_line){

                            // if the line index array is full, we make a new index array.
                            // the length of the new index array is the length of the old
                            // index array + 32
                            // at the end we copy the old index array into the new one and work
                            // further with the new one
                            if(line_count == line_index_.length){
                                int[] new_index = new int[line_count + 32];
                                System.arraycopy(line_index_, 0, new_index, 0, line_count);
                                line_index_ = new_index;
                            }

                            // we assign the index in the original char array as the
                            // start of the new line and increment the line counter
                            line_index_[line_count] = char_count;
                            line_count++;

                            // we handled the new line, so we set the new line flag to false
                            is_new_line = false;

                            // we set line started flag
                            is_line_started = false;
                        }

                        // if this is a new word we need to insert the word delimiter before
                        // the new word
                        if(is_new_word){

                            //if the line has been started already add word delimiter,
                            // otherwise we don't want to add word delimiter in front of the first
                            // word
                            if(is_line_started){

                                // if the chars array is full, we make a new chars array.
                                // the length of the new chars array is the length of the old
                                // index array + 2048
                                // at the end we copy the old index array into the new one and work
                                // further with the new one
                                if(char_count == chars_.length){
                                    char[] new_chars = new char[char_count + 2048];
                                    System.arraycopy(chars_, 0, new_chars, 0, char_count);
                                    chars_ = new_chars;
                                }

                                // we add the word delimiter in the chars array
                                chars_[char_count] = ' ';
                                char_count++;
                            }

                            // we handled the new word, so we set the new word flag to false
                            is_new_word = false;
                        }

                        // now we want to add the chracter to the chars array

                        // if the chars array is full, we make a new chars array.
                        // the length of the new chars array is the length of the old
                        // index array + 2048
                        // at the end we copy the old index array into the new one and work
                        // further with the new one
                        if(char_count == chars_.length){
                            char[] new_chars = new char[char_count + 2048];
                            System.arraycopy(chars_, 0, new_chars, 0, char_count);
                            chars_ = new_chars;
                        }

                        // add the character
                        chars_[char_count] = (char) c;
                        char_count++;

                        // since we added at least one character we already
                        // started the new line
                        is_line_started = true;

                        break;
                }

                // read the next character
                c = in.read();
            }

            // set the size of the index array to the real number of lines
            if(line_count != line_index_.length){
                int[] new_index = new int[line_count];
                System.arraycopy(line_index_, 0, new_index, 0, line_count);
                line_index_ = new_index;
            }

            // set the size of the chars array to the real number of characters
            if(char_count != chars_.length){
                char[] new_chars = new char[char_count];
                System.arraycopy(chars_, 0, new_chars, 0, char_count);
                chars_ = new_chars;
            }

        }catch(FileNotFoundException exc){

            // handle the exception if the file could not be found
            exc.printStackTrace();
            System.err.println("KWIC Error: Could not open " + file + "file.");
            System.exit(1);

        }catch(IOException exc){

            // handle other system I/O exception
            exc.printStackTrace();
            System.err.println("KWIC Error: Could not read " + file + "file.");
            System.exit(1);

        }
    }


    public void circularShift(){


        circular_shifts_ = new int[2][256];

        // count of circular shifts
        int shift_count = 0;

        // iterate through lines and make circular shifts
        for(int i = 0; i < line_index_.length; i++){

            // end index of the i-th line
            int line_end = 0;

            // if i-th line is the last line then line end index is
            // the index of the last character
            if(i == (line_index_.length - 1))
                line_end = chars_.length;

                // otherwise line end index is starting index of the
                // next line
            else
                line_end = line_index_[i + 1];

            // iterate through characters of i-th line
            for(int j = line_index_[i]; j < line_end; j++){


                if((chars_[j] == ' ') || (j == line_index_[i])){


                    if(shift_count == circular_shifts_[0].length){

                        // copy the line number row
                        int[] tmp = new int[shift_count + 256];
                        System.arraycopy(circular_shifts_[0], 0, tmp, 0, shift_count);
                        circular_shifts_[0] = tmp;

                        // copy the indices row
                        tmp = new int[shift_count + 256];
                        System.arraycopy(circular_shifts_[1], 0, tmp, 0, shift_count);
                        circular_shifts_[1] = tmp;
                    }

                    // set the original line number
                    circular_shifts_[0][shift_count] = i;
                    // set the starting index of this circular shift
                    circular_shifts_[1][shift_count] = (j == line_index_[i]) ? j : j + 1;

                    // increment the shift count
                    shift_count++;
                }

            }
        }

        // set the columns size of shift matrix to the real number of shifts
        if(shift_count != circular_shifts_[0].length){

            // copy the line number row
            int[] tmp = new int[shift_count];
            System.arraycopy(circular_shifts_[0], 0, tmp, 0, shift_count);
            circular_shifts_[0] = tmp;

            // copy the indices row
            tmp = new int[shift_count];
            System.arraycopy(circular_shifts_[1], 0, tmp, 0, shift_count);
            circular_shifts_[1] = tmp;
        }

    }


    public void alphabetizing(){

        // initialize the alphabetized matrix
        alphabetized_ = new int[2][circular_shifts_[0].length];

        // count of alphabetized lines
        int alphabetized_count = 0;

        // we use binary search to find the proper place
        // to insert a line,
        // declare variables for binary search
        int low = 0;
        int high = 0;
        int mid = 0;

        // process the circular shifts
        for(int i = 0; i < alphabetized_[0].length; i++){

            // the index of original line
            int line_number = circular_shifts_[0][i];

            // the start of the i-th shift
            int shift_start = circular_shifts_[1][i];

            // the start of the original line
            int line_start = line_index_[line_number];

            // the end of the original line
            int line_end = 0;

            // if the original line is the last line than line end index is
            // the index of the last character
            if(line_number == (line_index_.length - 1))
                line_end = chars_.length;

                // otherwise line end index is starting index of the
                // next line
            else
                line_end = line_index_[line_number + 1];

            // current shift array
            char[] current_shift = new char[line_end - line_start];

            // compose the current shift into array
            // compose a "real" shift
            if(line_start != shift_start){
                System.arraycopy(chars_, shift_start, current_shift, 0, line_end - shift_start);
                current_shift[line_end - shift_start] = ' ';
                System.arraycopy(chars_, line_start, current_shift, line_end - shift_start + 1, shift_start - line_start - 1);

                // compose the original line
            }else
                System.arraycopy(chars_, line_start, current_shift, 0, line_end - line_start);

            // binary search to the right place to insert
            // the i-th line
            low = 0;
            high = alphabetized_count - 1;
            while(low <= high){

                // find the mid line
                mid = (low + high) / 2;

                // the index of original mid line
                int mid_line_number = alphabetized_[0][mid];

                // the start of the mid shift
                int mid_shift_start = alphabetized_[1][mid];

                // the start of the original mid line
                int mid_line_start = line_index_[mid_line_number];

                // the end of the original mid line
                int mid_line_end = 0;

                // if the original mid line is the last line than line end index is
                // the index of the last character
                if(mid_line_number == (line_index_.length - 1))
                    mid_line_end = chars_.length;

                    // otherwise mid line end index is starting index of the
                    // next line
                else
                    mid_line_end = line_index_[mid_line_number + 1];

                // current mid line array
                char[] mid_line = new char[mid_line_end - mid_line_start];

                // compose the mid line into array
                // compose if mid line is a "real" shift
                if(mid_line_start != mid_shift_start){
                    System.arraycopy(chars_, mid_shift_start, mid_line, 0, mid_line_end - mid_shift_start);
                    mid_line[mid_line_end - mid_shift_start] = ' ';
                    System.arraycopy(chars_, mid_line_start, mid_line, mid_line_end - mid_shift_start + 1,
                            mid_shift_start - mid_line_start - 1);

                    // compose the mid if original line
                }else
                    System.arraycopy(chars_, mid_line_start, mid_line, 0, mid_line_end - mid_line_start);

                // find the smaller number of characters between mid and current shift
                int length = (current_shift.length < mid_line.length)
                        ? current_shift.length : mid_line.length;

                int compared = 0;

                for(int j = 0; j < length; j++){
                    if(current_shift[j] > mid_line[j]){
                        compared = 1;
                        break;
                    }else if(current_shift[j] < mid_line[j]){
                        compared = -1;
                        break;
                    }
                }


                if(compared == 0){
                    if(current_shift.length < mid_line.length)
                        compared = -1;
                    else if(current_shift.length > mid_line.length)
                        compared = 1;
                }

                switch(compared){
                    case 1: // i-th line greater
                        low = mid + 1;
                        break;
                    case -1: // i-th line smaller
                        high = mid - 1;
                        break;
                    default: // i-th line equal
                        low = mid;
                        high = mid - 1;
                        break;
                }
            }

            System.arraycopy(alphabetized_[0], low, alphabetized_[0], low + 1, alphabetized_count - low);
            System.arraycopy(alphabetized_[1], low, alphabetized_[1], low + 1, alphabetized_count - low);


            alphabetized_[0][low] = line_number;
            alphabetized_[1][low] = shift_start;

            alphabetized_count++;
        }
    }



    public void output(){
        for(int i = 0; i < alphabetized_[0].length; i++){
            int line_number = alphabetized_[0][i];
            int shift_start = alphabetized_[1][i];
            int line_start = line_index_[line_number];
            int line_end = 0;
            if(line_number == (line_index_.length - 1))
                line_end = chars_.length;
            else
                line_end = line_index_[line_number + 1];
            if(line_start != shift_start){
                for(int j = shift_start; j < line_end; j++)
                    System.out.print(chars_[j]);
                System.out.print(' ');
                for(int j = line_start; j < (shift_start - 1); j++)
                    System.out.print(chars_[j]);
            }else
                for(int j = line_start; j < line_end; j++)
                    System.out.print(chars_[j]);
            System.out.print('\n');
        }
    }


    public static void main(String[] args){
        KWIC kwic = new KWIC();
        if(args.length != 1){
            System.err.println("KWIC Usage: java kwic.ms.KWIC file_name");
            System.exit(1);
        }
        kwic.input(args[0]);
        kwic.circularShift();
        kwic.alphabetizing();
        kwic.output();
    }

}
