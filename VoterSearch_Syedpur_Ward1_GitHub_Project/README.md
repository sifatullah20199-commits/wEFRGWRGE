
# ভোটার তালিকা অনুসন্ধান — Android APK

এই project-টি ১ নং সৈয়দপুর ইউনিয়নের ১ নং ওয়ার্ডের একটি পূর্ণাঙ্গ Excel voter list থেকে তৈরি করা default CSV দিয়ে শুরু হয়।

## App features

- শুধু ৩টি search option:
  1. নাম
  2. বাবার নাম
  3. মায়ের নাম
- ঠিকানা বা ভোটার নম্বর দিয়ে search নেই।
- Offline SQLite database.
- APK-এর সঙ্গে default voter CSV থাকে।
- `CSV তালিকা পরিবর্তন করুন` থেকে ফোনের নতুন CSV নির্বাচন করা যায়।
- নতুন CSV import করলে পুরোনো database সম্পূর্ণ replace হয়।
- বাংলা CSV/Excel থেকে তৈরি UTF-8 CSV support.
- CSV header English অথবা common Bengali header হলে importer চিনতে পারে।
- Modern minimal Material 3 interface.
- Bengali text-এর জন্য Android system sans-serif/Noto fallback ব্যবহার করা হয়েছে; আলাদা font file না থাকায় missing-font সমস্যা হবে না।

## Expected CSV columns

The default file uses:

`Serial, Name, Voter No, Father's Name, Mother's Name, Address, Ward, Gender`

Search only uses:

`Name`, `Father's Name`, `Mother's Name`

The remaining columns are shown only in the voter details screen.

## GitHub থেকে APK build — Android Studio ছাড়াই

1. GitHub-এ নতুন repository তৈরি করুন, যেমন `voter-search`.
2. এই project-এর সব file repository-তে upload করুন।
3. `app/src/main/assets/default_voters.csv`-এ default list থাকবে।
4. GitHub-এ **Actions** tab খুলুন।
5. **Build APK** workflow নির্বাচন করুন।
6. **Run workflow** চাপুন।
7. Build শেষ হলে workflow-এর **Artifacts** অংশ থেকে `voter-search-debug-apk` download করুন।
8. ZIP খুলে `app-debug.apk` ফোনে install করুন।

### গুরুত্বপূর্ণ

- Voter data-সহ repository **Public করবেন না**। Private repository ব্যবহার করুন।
- Default CSV APK-এর ভেতরে bundled থাকে; নতুন CSV app-এর ভিতর থেকে import করলে পুরোনো data replace হয়।
- Debug APK personal/local distribution-এর জন্য উপযোগী। Public Play Store release-এর আগে signed release build তৈরি করতে হবে।
