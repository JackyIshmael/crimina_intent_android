package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import java.util.*
import androidx.lifecycle.Observer

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = "0" // 申请拿到一个结果
private const val REQUEST_CONTACT = "1" // 申请拿到联系人
private const val ARG_DATE = "date"
private const val DATE_FORMAT = "EEE, MM, dd"

class CrimeFragment : Fragment() {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var actualResult: Date
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button

    /**
     * 用库实现快速获取viewModel
     */
    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * 监听实现，每个请求都需单独在onCreate中注册
         */
        setFragmentResultListener(REQUEST_DATE) { requestKey, bundle ->
            actualResult = (bundle.get(ARG_DATE) as? Date)!!
            crime.date = actualResult
            updateUI()
            Log.d("saber", "我们拿到了 $actualResult,检查对应请求key$requestKey")
        }


        crime = Crime() // 手动调用时 title是默认值
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(ARG_CRIME_ID, "我们现在能拿到activity切换fragment时的crime id了，$crimeId")
        // 除了 crimeId，CrimeFragment中 crime的其他内容全都是默认值
        crimeDetailViewModel.loadCrime(crimeId) // 接下来我们可以观测crimeLiveData了
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button

        return view
    }

    /**
     *  观察数据变化，一有新数据发布就更新UI
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 直接操作viewmodel即可
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // TODO
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // TODO
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        /**
         * 点击该按钮后，启动一个新的  fragment （日期选择）
         * 按照正常的fragment交互，假设我们在启动这个fragment时就是为了拿到某个回调结果。
         */
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date, REQUEST_DATE).apply {
//                this@CrimeFragment.parentFragmentManager.setFragmentResultListener()
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                // 使用选择器显式响应,接收参数为初始intent以及选择器显示的提示文字
                val chooseIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooseIntent)
            }
        }

        suspectButton.apply {
            /***
             * 使用隐式intent唤起，从联系人应用中选择嫌疑人
             * 参数1：Intent范式，表示获取数据
             * 参数2：表示联系人app的跳链URI？
             * 所以这2个参数一加，和隐式唤起有什么关系了呢
             */
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                getResult.launch(pickContactIntent) // 设置点击监听器
            }

            // 以下代码可用于验证过滤器效果
//            pickContactIntent.addCategory(Intent.CATEGORY_HOME)

            // 调用操作系统的PackageManager，结合intent检查设备是否存在可用app，没有则将按钮置灰
            // ResoveInfo Flags可能的值：https://developer.android.com/reference/android/content/pm/PackageManager.ResolveInfoFlags
            val packageManager: PackageManager = requireActivity().packageManager;
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent,PackageManager.MATCH_DEFAULT_ONLY);
            if(resolvedActivity == null){
                isEnabled = false
            }

        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    /**
     * 定义一个ui刷新函数，用于观测到变动的liveData后进行UI刷新，否则UI就不动了！
     */
    private fun updateUI() {
        // titleField是可编辑对象，不能直接设置string，只能用setText
        titleField.setText(crime.title)
//        dateButton.text = crime.date.toString()
        dateButton.apply {
            text = DateFormat.format("EEE, MMM d, yyyy", crime.date)
        }
        // show一下kotlin的对象批量赋值操作，使用apply才能使Text被实际赋值
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState() // 跳过check动画
        }
//        错误示例：
//        if(crime.suspect.isEmpty()){
//            suspectButton.text = crime.suspect;
//        }
//        使用apply才能实际赋值
        if (!crime.suspect.isBlank()) {
            suspectButton.apply {
                text = crime.suspect
            }
        }

    }

    /**
     * 根据crime现状，匹配字符串，获取报告内容
     */
    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val contactUri: Uri? = it.data!!.data
                // 表示查询语句的数据源
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // 执行查询语句，获取的数据存储在cursor
                val cursor = contactUri?.let { it1 ->
                    requireActivity().contentResolver.query(
                        it1,
                        queryFields,
                        null,
                        null,
                        null
                    )
                }
                // kotlin的api，对cursor变量做一些数据处理
                cursor.use {
                    if (it?.count!! > 0) {
                        it.moveToFirst() // 取出数据中的第一行（因为cursor本身也就只包含一条记录
                        val suspect = it.getString(0)
                        crime.suspect = suspect
                        crimeDetailViewModel.saveCrime(crime)
                        suspectButton.text = suspect
                    }
                }
            }
        }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply { // 执行一个以参数传入的函数，该函数在当前对象上面执行，并返回当前对象
                // 创建bundle实例，写入内容
                putSerializable(ARG_CRIME_ID, crimeId)
            }

            return CrimeFragment().apply {
                arguments = args
            }

        }
    }

}