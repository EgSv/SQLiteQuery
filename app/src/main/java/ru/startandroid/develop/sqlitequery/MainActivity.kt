package ru.startandroid.develop.sqlitequery

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup

const val LOG_TAG = "myLogs"

class MainActivity : AppCompatActivity(), OnClickListener {

    private var name = arrayListOf<String>("Китай", "США", "Бразилия", "Россия", "Япония",
        "Германия", "Египет", "Италия", "Франция", "Канада")

    private var people = arrayListOf<Int>(1400, 311, 195, 142, 128, 82, 80, 60, 66, 35 )

    private var region = arrayListOf<String>("Азия", "Америка", "Америка", "Европа", "Азия",
        "Европа", "Африка", "Европа", "Европа", "Америка")

    private var btnAll:Button? = null
    private var btnFunc:Button? = null
    private var btnPeople:Button? = null
    private var btnShort:Button? = null
    private var btnGroup:Button? = null
    private var btnHaving:Button? = null

    private var etFunc:EditText? = null
    private var etPeople:EditText? = null
    private var etRegionPeople:EditText? = null

    private var rgShort:RadioGroup? = null

    private var dbHelper: DBHelper? = null

    private var db:SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAll = findViewById(R.id.btnAll)
        btnAll?.setOnClickListener(this)

        btnFunc = findViewById(R.id.btnFunc)
        btnFunc?.setOnClickListener(this)

        btnPeople = findViewById(R.id.btnPeople)
        btnPeople?.setOnClickListener(this)

        btnShort = findViewById(R.id.btnSort)
        btnShort?.setOnClickListener(this)

        btnGroup = findViewById(R.id.btnGroup)
        btnGroup?.setOnClickListener(this)

        btnHaving = findViewById(R.id.btnHaving)
        btnHaving?.setOnClickListener(this)

        etFunc = findViewById(R.id.etFunc)
        etPeople = findViewById(R.id.etPeople)
        etRegionPeople = findViewById(R.id.etRegionPeople)

        rgShort = findViewById<RadioGroup>(R.id.rgSort)

        dbHelper = DBHelper(this)

        db = dbHelper?.getWritableDatabase()

        val c:Cursor? = db?.query("mytable", null, null, null, null, null, null)
        if (c?.count == 0) {
            val cv = ContentValues()

            for (i in 0..9) {
                cv.put("name", name[i])
                cv.put("people", people[i])
                cv.put("region", region[i])

                Log.d(LOG_TAG, "id = ${db?.insert("mytable", null, cv)}")
            }
        }
        c?.close()
        dbHelper?.close()
        onClick(btnAll)
    }

    override fun onClick(v: View?) {
        db = dbHelper?.writableDatabase

        val sFunc:String = etFunc?.text.toString()
        val sPeople:String = etPeople?.text.toString()
        val sRegionPeople:String = etRegionPeople?.text.toString()

        var columns:Array<String>? = null
        var selection:String? = null
        var selectionArgs:Array<String>? = null
        var groupBy:String? = null
        var having:String? = null
        var orderBy:String? = null

        var c:Cursor? = null

        when(v?.id) {
            R.id.btnAll -> {
                Log.d(LOG_TAG, "---Все записи: ---")
                c = db?.query("mytable", null, null, null, null, null, null)
            }
            R.id.btnFunc -> {
                Log.d(LOG_TAG, "---Функция: ---")
                columns = arrayOf<String>(sFunc)
                c = db?.query("mytable", columns, null, null, null, null, null)
            }
            R.id.btnPeople -> {
                Log.d(LOG_TAG, "---Наседение больше: ---")
                selection = "people > ?"
                selectionArgs = arrayOf<String>(sPeople)
                c = db?.query("mytable", null, selection, selectionArgs, null, null, null)
            }
            R.id.btnGroup -> {
                Log.d(LOG_TAG, "---Население по региону: ---")
                columns = arrayOf<String>("region", "sum(people) as people")
                groupBy = "region"
                having = "sum(people) > $sRegionPeople"
                c = db?.query("mytable", columns, null, null, groupBy, having, null)
            }
            R.id.btnSort -> {
                when(rgShort?.checkedRadioButtonId) {
                    R.id.rName -> {
                        Log.d(LOG_TAG, "---Сортировка по наименованию: ---")
                        orderBy = "name"
                    }
                    R.id.rPeople -> {
                        Log.d(LOG_TAG, "---Сортировка по населению: ---")
                        orderBy = "people"
                    }
                    R.id.rRegion -> {
                        Log.d(LOG_TAG, "---Сортировка по региону: ---")
                        orderBy = "region"
                    }
                }
                c = db?.query("mytable", null, null, null, null, null, orderBy)
            }
        }
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    var str = ""
                    for(cn in c.columnNames) {
                        str = str + ("$cn = ${c.getString(c.getColumnIndexOrThrow(cn))};")
                    }
                    Log.d(LOG_TAG, str)
                } while (c.moveToNext())
            }
            c.close()
        } else {
            Log.d(LOG_TAG, "Cursor is null")
            dbHelper?.close()
        }
    }

    internal inner class DBHelper(context: Context?) :
        SQLiteOpenHelper(context, "myDB", null, 1) {
        override fun onCreate(db: SQLiteDatabase?) {
            Log.d(LOG_TAG, "---onCreate database: ---")
            db?.execSQL("create table mytable ("
                    + "id integer primary key autoincrement," + "name text,"
                    + "people integer," + "region text" + ");")
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        }
        }
}