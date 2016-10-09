package com.comtip.sudokucolor;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    Random rand = new Random();
    int  bufferRand = 0;  // พักค่า random ของปุ่ม random

    TextView sudokuResult,statusTxt,nameGame;
    Button  sundokuGen,randomBT;
    Spinner spaceSpinner,difficultSpinner;
    LinearLayout sudokuLayout;
//    LinearLayout numberLayout;
    final String [] dataSpinner = {"3","4","5","6","7","8"};   // ตัด ,"9" ออกชั่วคราว
    final String [] difficultLevel = {"Normal","Hard","Very Hard"};
    int  diffLv = 0;

    int [] sudokuNum;  //  ตัวแปรหลักเก็บค่า  sudoku ตัวเลขที่ประมาลผลมาได้ทั้งหมด
    int  sudokuKey = 3;  //  ขนาดชอง sudoku เช่น  3*3 , 4*4  ,  6*6

    int []  sudokuRow;  //  เก็บค่า sudoku ตัวเลขในแต่ละแถวใช้ในการเปรียบเทียบ
    int[] sudokuBuffer; //   พักค่า sudou ตัวเลข ก่อนเอาไปลงในตัวแปรหลัก
    int checkPosition;  //   ใช้แค่ใน loop โอนค่าตัวแปรพักไปตัวแปรหลัก

    int [] numberAnswer; //  เก็บค่าที่จะใส่ใน input ตอบ
    int bufferAnswer = 0;  //  พักคำตอบ แสดงหน้าปุ่มกด
    boolean [] answerBoolean;  //  ไว้ใช้สำหรับเช็คคำตอบ
    String show = "";  // เฉลยทั้งหมด

    // ส่วนเวลา
    int  timeCount = 0;
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingWindgets();
    }

    public void  settingWindgets () {

        nameGame = (TextView) findViewById(R.id.nameGame);
        randomBT  = (Button) findViewById(R.id.randomBT);
        statusTxt = (TextView)findViewById(R.id.statusTxt);
        statusTxt.setBackgroundColor(Color.parseColor("#FFFFFF"));
        sudokuResult = (TextView)findViewById(R.id.sudokuResult);
        sudokuLayout = (LinearLayout) findViewById(R.id.sudokuLayout);
       // numberLayout = (LinearLayout) findViewById(R.id.numberLayout);

        // สร้าง Spinner ไว้เลือกขนาด Sudoku
        spaceSpinner = (Spinner) findViewById(R.id.spaceSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,dataSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spaceSpinner.setAdapter(adapter);
        spaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sudokuKey = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //  สร้าง Spinner สำหรับเลือกระดับความยาก
        difficultSpinner = (Spinner)findViewById(R.id.difficultSpinner);
        ArrayAdapter<String> diffAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,difficultLevel);
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultSpinner.setAdapter(diffAdapter);
        difficultSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 diffLv = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //  ปุ่มสั่งสร้าง Sudoku
        sundokuGen = (Button) findViewById(R.id.sudokuGen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sundokuGen.setBackground(getResources().getDrawable(R.drawable.greybt,null));
        } else  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sundokuGen.setBackground(getResources().getDrawable(R.drawable.greybt));
        } else {
            sundokuGen.setBackgroundDrawable(getResources().getDrawable(R.drawable.greybt));
        }

        sundokuGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               sudokuGenerate();

            }
        });

    }


      //  ======= สร้าง sudoku ======
    public void sudokuGenerate () {
        sudokuNum =  new int[sudokuKey*sudokuKey];   // กำหนดขนาด sudoku  จาก sudokuKey เช่น 3*3

        // แถว 1
        int []  bufferCompare = new  int [sudokuKey];   // สร้างตัว compare ใช้ในการเช็คเลขซ้ำ
        for (int i = 0; i < sudokuKey;i++) {
            bufferCompare[i] = i+1;
        }

//         Random  rd = new Random();
         for (int i = 0; i < sudokuKey; i++){
             sudokuNum[i] = sudokuKey+1;     // ใส่ค่า dummy หลอกๆ ไว้ ก่อนเข้า method เปรียบเทียบเลขซ้ำ
             while (!filterNumber(sudokuNum[i],bufferCompare)){
                 sudokuNum[i] = rand.nextInt(sudokuKey)+1;        // ลูปจนกว่าจะได้ตัวเลขไม่ซ้ำก่อนหน้านี้
             }
         }


        //////////////////////////
        //  แถว 2  ถึงแถวสุดท้าย
        sudokuRow =  new int [sudokuKey*(sudokuKey-1)];   // กำหนดขนาดตัวแปรที่จะใช้ในการเปรียบเลขระหว่างแถว (ขนาด *  (ขนาด-1)) ที่ -1 เพราะ แถวแรกได้ถูกสร้างมาเรียบร้อยแล้ว
        for (int checkRow = 1; checkRow < sudokuKey; checkRow++)  {
            for (int i = 0; i < sudokuKey*checkRow;i++) {
                sudokuRow[i] = sudokuNum[i];      // copy ข้อมูลจากตัวแปรหลัก ลงไปในตัวแปรเปรียบเทียบ
            }

            sudokuBuffer = filterRow(sudokuRow,sudokuKey,checkRow);   // สร้างแถวตัวเลขใหม่ และกรองตัวเลขซ้ำกันแถวก่อนหน้านี้ทุกแถว

            checkPosition = 0;
            for (int i = (sudokuKey*checkRow);i < sudokuKey*(checkRow+1); i++) {
                sudokuNum[i] = sudokuBuffer[checkPosition];  //  copy ข้อมูลแถวใหม่ที่ไม่มีตัวเลขซ้ำทั้งแถวและคอลัมน์ลงตัวแปรหลัก
                checkPosition = checkPosition+1;
            }
        }

        // ใช้ในการแสดงผลเฉลย
        show = "";
        int row = 0;

        for (int i = 0; i < sudokuKey*sudokuKey;i++) {

              show += " " + sudokuNum[i];
              row = row + 1;

            if (row == sudokuKey) {
                row = 0;
                show +="\n";
            }

        }

        layoutGenerate();

    }


    //filter กรองตัวเลขไม่ให้ซ้ำกัน
    public static boolean filterNumber (int item ,int[] compare) {
        for (int i=0;i <  compare.length;i++ ){
            if(item == compare[i]) {    //ถ้าตัวเลขที่สุ่มมาซ้ำกับข้อมูลใน compare  แสดงว่าเลขนั้นไม่ซ้ำกับในแถว  เอาไปใช้งานได้
                compare[i] = 0;         // จากนั้นแทนค่า compare เลขนั้นด้วย 0  (เลขนั้นต่อไปจะไม่มีให้เปรียบเทียบ  เพราะฉนั้นเกิดโอกาสไม่ซ้ำเสมอ)
                return  true;
            }
        }
        return  false;
    }


    //  เช็คแถว

    public static int[]  filterRow (int [] row ,int key ,int loop){

        boolean  checkRow = false;
        int[] buffer = new int[key];       // พักข้อมูลตัวเลขทั้งแถวที่ได้จากการสุ่ม
        int[]  compare = new int [key];   //  ตัวเลขที่จะใช้ในการเปรียบเทียบเลขซ้ำ ในแต่ละแถว
        Random random = new Random();

        while(!checkRow) {  // จนกว่าเลขแถวใหม่ที่สุ่มสร้างขึ้นมา จะไม่มีจุดไหนซ้ำกับแถวก่อนหน้านี้ทุกแถว

            for (int i = 0; i < key; i++) {
                compare[i] = i + 1;       // สร้าง compare
            }

            for (int i = 0; i < key; i++) {
                buffer[i] = key + 1;
                while (!filterNumber(buffer[i], compare)) {
                    buffer[i] = random.nextInt(key) + 1;         //สุ่มสร้าง เลขแถวใหม่ โดยเลขแต่ละตัวในแถวนั้นจะไม่ซ้ำกัน
                }
            }
              // เปรียบเทียบเลขระหว่างแถว ไม่ให้ซ้ำกัน
            checkRow = true;
            for (int i = 0; i < key; i++) {     //  loop เป็นตัวแปรกำหนดว่าจะเปรียบเทียบกับแถวก่อนหน้านี้กี่แถว
                if(loop == 1) {
                    if (buffer[i] == row[i]) {
                        checkRow = false;
                    }
                }

                if (loop == 2) {
                    if  ((buffer[i] == row[i]) || (buffer[i] == row[i + key])) {
                        checkRow = false;
                    }
                }

                if (loop == 3) {
                    if  ((buffer[i] == row[i]) || (buffer[i] == row[i + key]) || (buffer[i] == row[i+(key*2)])) {
                        checkRow = false;
                    }
                }

                if (loop == 4) {
                    if  ((buffer[i] == row[i]) || (buffer[i] == row[i + key]) || (buffer[i] == row[i+(key*2)])
                            || (buffer[i] == row[i+(key*3)])   ) {
                        checkRow = false;
                    }
                }

                if (loop == 5) {
                    if  ((buffer[i] == row[i]) || (buffer[i] == row[i + key]) || (buffer[i] == row[i+(key*2)])
                            || (buffer[i] == row[i+(key*3)]) || (buffer[i] == row[i+(key*4)])  ) {
                        checkRow = false;
                    }
                }

                if (loop == 6) {
                    if  ((buffer[i] == row[i]) || (buffer[i] == row[i + key]) || (buffer[i] == row[i+(key*2)])
                            || (buffer[i] == row[i+(key*3)]) || (buffer[i] == row[i+(key*4)])
                            || (buffer[i] == row[i+(key*5)])
                            ) {
                        checkRow = false;
                    }
                }

                if (loop == 7) {
                    if  ((buffer[i] == row[i]) || (buffer[i] == row[i + key]) || (buffer[i] == row[i+(key*2)])
                            || (buffer[i] == row[i+(key*3)]) || (buffer[i] == row[i+(key*4)])
                            || (buffer[i] == row[i+(key*5)])|| (buffer[i] == row[i+(key*6)])
                            ) {
                        checkRow = false;
                    }
                }

                if (loop == 8) {
                    if  ((buffer[i] == row[i]) || (buffer[i] == row[i + key]) || (buffer[i] == row[i+(key*2)])
                            || (buffer[i] == row[i+(key*3)]) || (buffer[i] == row[i+(key*4)])
                            || (buffer[i] == row[i+(key*5)])|| (buffer[i] == row[i+(key*6)]) || (buffer[i] == row[i+(key*7)])
                            ) {
                        checkRow = false;
                    }
                }

            }
        }


        return buffer;
    }

    // สร้าง Layout ฝั่ง ปริศนา
    public void layoutGenerate(){

        //////////////////////////////////////
        //========ส่วนสร้างวางตำแหน่งตัวเลขปริศนา================

        // กำหนดระดับความยาก
        int  difficultLevel = 0;
        switch (diffLv) {
            case 0 :
                difficultLevel = sudokuNum.length/2;
                nameGame.setText("  Sudoku Color Normal Mode Level "+sudokuKey);
                break;
            case 1  :
                difficultLevel = (sudokuNum.length/2) + (((sudokuNum.length/2)/2)/2);
                nameGame.setText("  Sudoku Color Hard Mode Level "+sudokuKey);
                break;
            case 2  :
                difficultLevel = (sudokuNum.length/2) + ((sudokuNum.length/2)/2);
                nameGame.setText("  Sudoku Color Very Hard Mode Level "+sudokuKey);
                break;
        }


        answerBoolean = new boolean[sudokuKey*sudokuKey]; //  กำหนดขนาด Array Boolean  ไว้สำหรับเช็คคำตอบ

        int [] questPosition = new int[difficultLevel];    // จำนวนข้อมูลตำแหน่งตัวเลขปริศนา  ผันแปรตามระดับความยาก

        int []  bufferQuest = new  int [sudokuNum.length];   // สร้างตัว compare ใช้ในการเช็คตำแหน่งซ้ำ
        for (int i = 0; i < sudokuNum.length;i++) {
            bufferQuest[i] = i+1;
        }

         for (int i = 0; i < questPosition.length;i++){  // ใส่ค่าประจำตำแหน่งคำถาม ต้องไม่ซ้ำตำแหน่งเดิม
             questPosition[i] = sudokuNum.length+1;
             while(!filterNumber(questPosition[i],bufferQuest)) {
               questPosition[i] = rand.nextInt(sudokuNum.length);
             }
         }

         ////////////////////////////////////

         //==== สร้าง Layout ===== มี sudokuLayout คัว layout แม่ ใช้ xml สร้าง  นอกนั้น  programmatically ล้วนๆ

           int   position = 0;   //  index ข้อมูลตำแหน่งจริง

           if (sudokuLayout.getChildCount() >0 ) {   // ถ้า layout แม่ มีลูกข้างในให้ทำการเคลียร์ทิ้งให้หมดก่อน
               sudokuLayout.removeAllViews();
           }

         for (int i = 0; i < sudokuKey;i++){  // สร้าง layout ลูก ไว้รองรับ  button
             LinearLayout  rowS = new LinearLayout(this);
             rowS.setOrientation(LinearLayout.HORIZONTAL);
             rowS.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

             for (int j = 0 ; j < sudokuKey;j++) {

                 final Button bt = new Button(this);
                 bt.setLayoutParams(new ViewGroup.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                 bt.setId(position);  // ใส่ id เพื่อกำหนด button แต่ละอันเป็นคนละตัว
                      // ตรวจสอบตำแหน่งว่าตรงตำแหน่งที่จะเป็นคำถามหรือเปล่า
                      if (filterQuest(position,questPosition)) {
                          answerBoolean[position] = false;   // กรณีเป็นตำแหน่งเลขปริศนา จะใส่ค่า false ลง boolean ประจำตำแหน่ง
                          final int answers = sudokuNum[position];   // เก็บข้อมูลเฉลย
                          final int  answerPosition = position;     //  เก็บตำแหน่งข้อมูล
                          bt.setText("?");


                          // เช็คเวอร์ชั่น  deprecated code     สีขาว คือ ปุ่มปริศนา
                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                              bt.setBackground(getResources().getDrawable(R.drawable.whitebt,null));
                          } else  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                              bt.setBackground(getResources().getDrawable(R.drawable.whitebt));

                          } else {
                              bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitebt));
                              }

                          //  กำหนด Listener คลิ๊กปุ่ม
                          bt.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {

                                  if (bufferAnswer == 99) {  // ปุ่มพิเศษทำงาน ไม่ว่าค่าจะเป็นอะไร จะเฉลยคำตอบที่ถูกเสมอ
                                      bufferAnswer  = answers;
                                  }

                                  if(bufferAnswer != 0) { // ถ้าข้อมูลที่รับเข้ามาผ่าน bufferAnswer มีค่าไม่เท่ากับ 0  ปุ่มจะแสดงผลลัพธ์ สีประจำตัวเลข
                                      ///===============
                                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                          switch(bufferAnswer) {
                                              case 1:bt.setBackground(getResources().getDrawable(R.drawable.blackbt,null));  break;
                                              case 2:bt.setBackground(getResources().getDrawable(R.drawable.brownbt,null));  break;
                                              case 3:bt.setBackground(getResources().getDrawable(R.drawable.redbt,null));  break;
                                              case 4:bt.setBackground(getResources().getDrawable(R.drawable.orangebt,null));  break;
                                              case 5:bt.setBackground(getResources().getDrawable(R.drawable.yellowbt,null));  break;
                                              case 6:bt.setBackground(getResources().getDrawable(R.drawable.greenbt,null));  break;
                                              case 7:bt.setBackground(getResources().getDrawable(R.drawable.bluebt,null));  break;
                                              case 8:bt.setBackground(getResources().getDrawable(R.drawable.purplebt,null));  break;
                                              case 9:bt.setBackground(getResources().getDrawable(R.drawable.greybt,null));  break;
                                          }


                                      } else  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                          switch(bufferAnswer) {
                                              case 1:bt.setBackground(getResources().getDrawable(R.drawable.blackbt));  break;
                                              case 2:bt.setBackground(getResources().getDrawable(R.drawable.brownbt));  break;
                                              case 3:bt.setBackground(getResources().getDrawable(R.drawable.redbt));  break;
                                              case 4:bt.setBackground(getResources().getDrawable(R.drawable.orangebt));  break;
                                              case 5:bt.setBackground(getResources().getDrawable(R.drawable.yellowbt));  break;
                                              case 6:bt.setBackground(getResources().getDrawable(R.drawable.greenbt));  break;
                                              case 7:bt.setBackground(getResources().getDrawable(R.drawable.bluebt));  break;
                                              case 8:bt.setBackground(getResources().getDrawable(R.drawable.purplebt));  break;
                                              case 9:bt.setBackground(getResources().getDrawable(R.drawable.greybt));  break;
                                          }


                                      } else {

                                          switch(bufferAnswer) {
                                              case 1:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.blackbt));  break;
                                              case 2:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.brownbt));  break;
                                              case 3:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.redbt));  break;
                                              case 4:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.orangebt));  break;
                                              case 5:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellowbt));  break;
                                              case 6:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.greenbt));  break;
                                              case 7:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bluebt));  break;
                                              case 8:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.purplebt));  break;
                                              case 9:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.greybt));  break;
                                          }

                                      }

                                      ///==================
                                      bt.setText("###");   //  เครื่องหมายกำกับไว้ให้รู้นี่เป็นตำแหน่งปริศนา

                                      ///////////   เช็คคำตอบทั้งหมด ทุกตำแหน่งต้องเป็น true ถึงจะผ่าน
                                      if(bufferAnswer == answers) {   //  หากคำตอบจริง กับ คำตอบที่รับเข้ามา เท่ากัน   จะใส่ค่า true ลง boolean ประจำตำแหน่ง
                                          answerBoolean[answerPosition] = true;
                                          if(checkingAnswers(answerBoolean)){
                                              timer.cancel();
                                              randomBT.setVisibility(View.INVISIBLE);
                                              nameGame.setText("    All Clear !!! ");
                                                if (timeCount > 60)  {
                                                  sudokuResult.setText("ใช้เวลา\n"+  (timeCount/60)+" นาที \n" +(timeCount%60)+" วินาที\n"+show );
                                                } else {
                                                  sudokuResult.setText("ใช้เวลา\n" + timeCount + " วินาที\n" + show);
                                                }
                                              bufferAnswer = 1000;   //  dummy value ใส่ไว้เพือล็อคไม่ให้ทำการใดๆต่อได้ หลังจากเคลียร์เกมนั้นจบแล้ว  นอกจากสร้างเกมใหม่
                                          }
                                      } else {
                                          answerBoolean[answerPosition] = false;  //  ไม่เท่ากัน ก้ใส่ค่า false ไป *ครอบคลุมกรณีตอบถูกแล้วเปลี่ยนใจไปตอบผิดไว้ด้วย*
                                      }
                                      /////////
                                      if(bufferAnswer != 1000) {bufferAnswer = 0;}  // reset ค่า bufferAnswer ให้เป็น 0
                                      statusTxt.setText("???");
                                      statusTxt.setTextColor(Color.parseColor("#000000"));
                                      statusTxt.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                  } else{
                                      // กรณี bufferAnswer เป็น 0 จะ reset ผลลัพธ์หน้าปุ่มเป็น ?  และปุ่มเปลี่ยนเป็นสีขาว
                                      //===========================
                                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                          bt.setBackground(getResources().getDrawable(R.drawable.whitebt,null));
                                      } else  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                          bt.setBackground(getResources().getDrawable(R.drawable.whitebt));
                                      } else {
                                          bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitebt));
                                      }
                                      //==========================
                                      bt.setText("?");
                                      answerBoolean[answerPosition] = false;
                                  }

                              }

                          }
                          );


                      } else {
                          answerBoolean[position] = true;  // ถ้าไม่ใช่ตำแหน่งเลขปริศนา  boolean จะเป็น true
                        //  bt.setText("" + sudokuNum[position]);   // เลขประจำตำแหน่งปุ่ม

                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                              switch(sudokuNum[position]) {
                                  case 1:bt.setBackground(getResources().getDrawable(R.drawable.blackbt,null));  break;
                                  case 2:bt.setBackground(getResources().getDrawable(R.drawable.brownbt,null));  break;
                                  case 3:bt.setBackground(getResources().getDrawable(R.drawable.redbt,null));  break;
                                  case 4:bt.setBackground(getResources().getDrawable(R.drawable.orangebt,null));  break;
                                  case 5:bt.setBackground(getResources().getDrawable(R.drawable.yellowbt,null));  break;
                                  case 6:bt.setBackground(getResources().getDrawable(R.drawable.greenbt,null));  break;
                                  case 7:bt.setBackground(getResources().getDrawable(R.drawable.bluebt,null));  break;
                                  case 8:bt.setBackground(getResources().getDrawable(R.drawable.purplebt,null));  break;
                                  case 9:bt.setBackground(getResources().getDrawable(R.drawable.greybt,null));  break;
                              }


                          } else  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                              switch(sudokuNum[position]) {
                                  case 1:bt.setBackground(getResources().getDrawable(R.drawable.blackbt));  break;
                                  case 2:bt.setBackground(getResources().getDrawable(R.drawable.brownbt));  break;
                                  case 3:bt.setBackground(getResources().getDrawable(R.drawable.redbt));  break;
                                  case 4:bt.setBackground(getResources().getDrawable(R.drawable.orangebt));  break;
                                  case 5:bt.setBackground(getResources().getDrawable(R.drawable.yellowbt));  break;
                                  case 6:bt.setBackground(getResources().getDrawable(R.drawable.greenbt));  break;
                                  case 7:bt.setBackground(getResources().getDrawable(R.drawable.bluebt));  break;
                                  case 8:bt.setBackground(getResources().getDrawable(R.drawable.purplebt));  break;
                                  case 9:bt.setBackground(getResources().getDrawable(R.drawable.greybt));  break;
                              }


                          } else {

                              switch(sudokuNum[position]) {
                                  case 1:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.blackbt));  break;
                                  case 2:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.brownbt));  break;
                                  case 3:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.redbt));  break;
                                  case 4:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.orangebt));  break;
                                  case 5:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellowbt));  break;
                                  case 6:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.greenbt));  break;
                                  case 7:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bluebt));  break;
                                  case 8:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.purplebt));  break;
                                  case 9:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.greybt));  break;
                              }


                          }
                      }


                 position = position+1;
                 rowS.addView(bt);  //  button ที่สร้างมาทั้งแถว ยัดเข้า layout ลูก
             }
             sudokuLayout.addView(rowS);   // layout ลูกยัดเข้า layout แม่
         }


        numberlayoutGenerate();
    }


    //filter กรองตำแหน่งตัวเลขคำถามไม่ให้ซ้ำกัน
    public static boolean filterQuest (int item ,int[] compare) {
        for (int i=0;i <  compare.length;i++ ){
            if(item == compare[i]) {    //ถ้าตัวเลขที่สุ่มมาซ้ำกับข้อมูลใน compare  แสดงว่าเลขนั้นไม่ซ้ำกับในแถว  เอาไปใช้งานได้
                return  true;
            }
        }
        return  false;
    }


    //  สร้างปุ่มฝั่งคำตอบ
    public void numberlayoutGenerate () {

           //===== สำหรับปุ่ม Random ==========================
        randomBT.setVisibility(View.VISIBLE);
        bufferRand  = rand.nextInt(sudokuKey)+1;
        randomBT.setTextColor(Color.parseColor("#FFFFFF"));
        randomBT.setText("###");
         switch (bufferRand) {  // กำหนดสีปุ่มครังแรก
             case 1:randomBT.setBackgroundColor(Color.parseColor("#000000"));  break;
             case 2:randomBT.setBackgroundColor(Color.parseColor("#994C00"));  break;
             case 3:randomBT.setBackgroundColor(Color.parseColor("#FF0000"));  break;
             case 4:randomBT.setBackgroundColor(Color.parseColor("#FF9933"));  break;
             case 5:randomBT.setBackgroundColor(Color.parseColor("#FFFF00"));  break;
             case 6:randomBT.setBackgroundColor(Color.parseColor("#00FF00"));  break;
             case 7:randomBT.setBackgroundColor(Color.parseColor("#0000FF"));  break;
             case 8:randomBT.setBackgroundColor(Color.parseColor("#990099"));  break;
             case 9:randomBT.setBackgroundColor(Color.parseColor("#C0C0C0"));  break;
         }


           randomBT.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   bufferAnswer =  bufferRand;
                   
                       statusTxt.setText("       ");
                       switch(bufferAnswer) {  // แสดงสถานะปัจจุบันอยู่กับสีอะไร
                           case 1:statusTxt.setBackgroundColor(Color.parseColor("#000000"));  break;
                           case 2:statusTxt.setBackgroundColor(Color.parseColor("#994C00"));  break;
                           case 3:statusTxt.setBackgroundColor(Color.parseColor("#FF0000"));  break;
                           case 4:statusTxt.setBackgroundColor(Color.parseColor("#FF9933"));  break;
                           case 5:statusTxt.setBackgroundColor(Color.parseColor("#FFFF00"));  break;
                           case 6:statusTxt.setBackgroundColor(Color.parseColor("#00FF00"));  break;
                           case 7:statusTxt.setBackgroundColor(Color.parseColor("#0000FF"));  break;
                           case 8:statusTxt.setBackgroundColor(Color.parseColor("#990099"));  break;
                           case 9:statusTxt.setBackgroundColor(Color.parseColor("#C0C0C0"));  break;

                           case 99 :     // ปุ่มไอเท็มพิเศษ  แทนได้ทุกค่าสี
                               statusTxt.setBackgroundColor(Color.parseColor("#FFFFFF"));
                               statusTxt.setTextColor(Color.parseColor("#FF0000"));
                               statusTxt.setText("@@@");
                               break;
                       }


                   // random ค่าปุ่มสีต่อไป
                   bufferRand  = rand.nextInt(sudokuKey)+1;
                   switch (bufferRand) {
                       case 1:randomBT.setBackgroundColor(Color.parseColor("#000000"));  break;
                       case 2:randomBT.setBackgroundColor(Color.parseColor("#994C00"));  break;
                       case 3:randomBT.setBackgroundColor(Color.parseColor("#FF0000"));  break;
                       case 4:randomBT.setBackgroundColor(Color.parseColor("#FF9933"));  break;
                       case 5:randomBT.setBackgroundColor(Color.parseColor("#FFFF00"));  break;
                       case 6:randomBT.setBackgroundColor(Color.parseColor("#00FF00"));  break;
                       case 7:randomBT.setBackgroundColor(Color.parseColor("#0000FF"));  break;
                       case 8:randomBT.setBackgroundColor(Color.parseColor("#990099"));  break;
                       case 9:randomBT.setBackgroundColor(Color.parseColor("#C0C0C0"));  break;
                   }

                   if ((timeCount%(rand.nextInt(4)+7) == 0)&&(bufferAnswer != 99)) {  //  กันออกปุมพิเศษติดกัน     โอกาสออกช่าง 7,8,9,10 หารลงตัว
                        if(rand.nextInt(4) == 0 ) {  // สุ่มออกและไม่ออก กันโกงแอบนับเวลา  ตั้งระดับสุ่มออกอัตราออก 1 ใน 4
                            randomBT.setText("@@@");
                            randomBT.setTextColor(Color.parseColor("#FF0000"));
                            randomBT.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            bufferRand = 99;

                        } else {
                            randomBT.setTextColor(Color.parseColor("#FFFFFF"));
                            randomBT.setText("###");
                        }
                   } else {
                       randomBT.setTextColor(Color.parseColor("#FFFFFF"));
                       randomBT.setText("###");
                   }
               }
           });
        //==================================================

        // สำหรับปุ่มฝั่งขวา

        /*    ปิดชั่วคราว เพื่อ test แบบปุ่ม random ปุ่มเดียว
            numberAnswer = new int[sudokuKey];


            if (numberLayout.getChildCount() >0 ) {   // ถ้า layout แม่ มีลูกข้างในให้ทำการเคลียร์ทิ้งให้หมดก่อน
               numberLayout.removeAllViews();
            }


            for (int j = 0 ; j < sudokuKey;j++) {
                Button bt = new Button(this);
                bt.setLayoutParams(new ViewGroup.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                bt.setId(j);  // ใส่ id เพื่อกำหนด button แต่ละอันเป็นคนละตัว

                    final int positionAnswer = j;
                    numberAnswer[j] = j+1;
                 //   bt.setText("" + (j+1));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    switch(numberAnswer[j]) {
                        case 1:bt.setBackground(getResources().getDrawable(R.drawable.blackbt,null));  break;
                        case 2:bt.setBackground(getResources().getDrawable(R.drawable.brownbt,null));  break;
                        case 3:bt.setBackground(getResources().getDrawable(R.drawable.redbt,null));  break;
                        case 4:bt.setBackground(getResources().getDrawable(R.drawable.orangebt,null));  break;
                        case 5:bt.setBackground(getResources().getDrawable(R.drawable.yellowbt,null));  break;
                        case 6:bt.setBackground(getResources().getDrawable(R.drawable.greenbt,null));  break;
                        case 7:bt.setBackground(getResources().getDrawable(R.drawable.bluebt,null));  break;
                        case 8:bt.setBackground(getResources().getDrawable(R.drawable.purplebt,null));  break;
                        case 9:bt.setBackground(getResources().getDrawable(R.drawable.greybt,null));  break;
                    }


                } else  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    switch(numberAnswer[j]) {
                        case 1:bt.setBackground(getResources().getDrawable(R.drawable.blackbt));  break;
                        case 2:bt.setBackground(getResources().getDrawable(R.drawable.brownbt));  break;
                        case 3:bt.setBackground(getResources().getDrawable(R.drawable.redbt));  break;
                        case 4:bt.setBackground(getResources().getDrawable(R.drawable.orangebt));  break;
                        case 5:bt.setBackground(getResources().getDrawable(R.drawable.yellowbt));  break;
                        case 6:bt.setBackground(getResources().getDrawable(R.drawable.greenbt));  break;
                        case 7:bt.setBackground(getResources().getDrawable(R.drawable.bluebt));  break;
                        case 8:bt.setBackground(getResources().getDrawable(R.drawable.purplebt));  break;
                        case 9:bt.setBackground(getResources().getDrawable(R.drawable.greybt));  break;
                    }


                } else {

                    switch(numberAnswer[j]) {
                        case 1:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.blackbt));  break;
                        case 2:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.brownbt));  break;
                        case 3:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.redbt));  break;
                        case 4:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.orangebt));  break;
                        case 5:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellowbt));  break;
                        case 6:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.greenbt));  break;
                        case 7:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.bluebt));  break;
                        case 8:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.purplebt));  break;
                        case 9:bt.setBackgroundDrawable(getResources().getDrawable(R.drawable.greybt));  break;
                    }


                }


                //  กำหนด listener  เมื่อแตะปุ่มฝั่งคำตอบ  จะพักค่าไว้ที่ bufferAnswer เพื่อเอาไปใช้ในฝั่งคำถามต่อไป
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bufferAnswer = numberAnswer[positionAnswer];

                        /////////////
                        switch(bufferAnswer) {
                            case 1:statusTxt.setBackgroundColor(Color.parseColor("#000000"));  break;
                            case 2:statusTxt.setBackgroundColor(Color.parseColor("#994C00"));  break;
                            case 3:statusTxt.setBackgroundColor(Color.parseColor("#FF0000"));  break;
                            case 4:statusTxt.setBackgroundColor(Color.parseColor("#FF9933"));  break;
                            case 5:statusTxt.setBackgroundColor(Color.parseColor("#FFFF00"));  break;
                            case 6:statusTxt.setBackgroundColor(Color.parseColor("#00FF00"));  break;
                            case 7:statusTxt.setBackgroundColor(Color.parseColor("#0000FF"));  break;
                            case 8:statusTxt.setBackgroundColor(Color.parseColor("#990099"));  break;
                            case 9:statusTxt.setBackgroundColor(Color.parseColor("#C0C0C0"));  break;
                        }
                        ////////////

                    }
                });

                numberLayout.addView(bt);  //  button ที่สร้างมาทั้งแถว ยัดเข้า layout
            }

        */

        /// เริ่มจับเวลา ///
        timeCount = 0;
        timer.cancel(); // ไว้ฆ่า thread ก่อนหน้านี้ กัน Bug กรณีไม่แก้โจทย์เก่า ข้ามไปสร้างโจทย์ใหม่
        timer = new Timer();
        timer.schedule(new timeStage(),0,1000);
        ///////////////////
    }


    // เช็คคำตอบทุกตำแหน่ง   ทุกตำแหน่งต้องเป็น true หมดถึงผ่าน
    public  static boolean checkingAnswers (boolean [] check){
        for (int i=0;i <  check.length;i++ ){
            if(check[i] == false) {
                return  false;
            }
        }
        return  true;
    }


      // จับเวลา
      class timeStage extends TimerTask {
          @Override
          public void run () {
             MainActivity.this.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     if(timeCount == Integer.MAX_VALUE ) {  // กัน Bug ค่าเกิน
                         timeCount = 0;
                     }  else { timeCount = timeCount + 1;}


                     if (timeCount > 60)  {
                         sudokuResult.setText((timeCount/60)+" นาที\n" +(timeCount%60)+" วินาที" );
                     } else {
                         sudokuResult.setText(timeCount + " วินาที" );
                     }
                 }
             });
          }
      }

  }



