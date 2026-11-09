const $=id=>document.getElementById(id);
const defaults={scoreUrl:"https://cricketlivenow.com/match/13LT?layout=layout1&source=layout1-only&f=1&sf=2BD&time=1794198600000"};
const words={
ur:{
subtitle:"موبائل کنٹرول پینل · ورژن 1",heroTitle:"آپ کا میچ، آپ کی لائیو نشریات",heroText:"اسکور بورڈ کا لنک محفوظ کریں، میچ کا صفحہ کھولیں اور اپنی براڈکاسٹ سیٹنگز سنبھالیں۔",
scoreTitle:"اسکور بورڈ کا لنک",scoreSub:"آپ کا دیا ہوا لنک پہلے سے شامل ہے",scoreLabel:"CricketLiveNow URL",openScore:"اسکور بورڈ کھولیں ↗",saveUrl:"لنک محفوظ کریں",
embedNotice:"اگر ویب سائٹ iframe کی اجازت نہ دے تو اسکور بورڈ الگ ٹیب میں کھلے گا۔ اس کا مطلب یہ نہیں کہ ویڈیو خودکار طور پر نشر ہو رہی ہے۔",
destTitle:"اسٹریمنگ کے چینلز",destSub:"اپنے چینلز کی تفصیلات درج کریں",ytLabel:"YouTube Stream Key",ytPlaceholder:"YouTube سے Stream Key درج کریں",fbLabel:"Facebook Stream Key",fbPlaceholder:"Facebook سے Stream Key درج کریں",show:"دکھائیں",hide:"چھپائیں",
keyNotice:"Stream Keys پاس ورڈ کی طرح خفیہ رکھیں۔ یہ صرف اسی موبائل کے browser storage میں محفوظ ہوں گی؛ کسی کو نہ بھیجیں۔",saveSettings:"سیٹنگز محفوظ کریں",
liveTitle:"لائیو نشریات",liveSub:"براہِ راست نشریات کی تیاری",engineStatus:"اسٹریمنگ انجن منسلک نہیں",engineDetail:"یہ کنٹرول پینل ہے؛ RTMP relay/server درکار ہے۔",startLive:"● YouTube + Facebook لائیو شروع کریں",liveNotice:"Start بٹن ابھی بند ہے کیونکہ اس پیکج میں RTMP ویڈیو انجن یا کلاؤڈ سرور شامل نہیں۔ صرف Stream Keys درج کرنے سے لائیو شروع نہیں ہوتی۔",
installTitle:"موبائل پر انسٹال کریں",install1:"اس PWA کو HTTPS ویب ہوسٹنگ پر اپ لوڈ کریں۔",install2:"Android Chrome میں ویب سائٹ کھولیں۔",install3:"⋮ مینو سے Add to Home screen یا Install app منتخب کریں۔",footer:"AI Cricket Streamer · موبائل PWA پروٹوٹائپ\nمفت اوپن ویب ٹیکنالوجی پر مبنی",
saved:"سیٹنگز اسی موبائل میں محفوظ ہو گئیں",badUrl:"درست ویب URL درج کریں"
},
en:{
subtitle:"Mobile Control Panel · Version 1",heroTitle:"Your match, your live broadcast",heroText:"Save your scoreboard link, open the match page, and manage your broadcast settings.",
scoreTitle:"Scoreboard URL",scoreSub:"Your supplied link is already added",scoreLabel:"CricketLiveNow URL",openScore:"Open scoreboard ↗",saveUrl:"Save URL",
embedNotice:"If the website blocks iframe embedding, the scoreboard will open in a separate tab. This does not mean video is being streamed automatically.",
destTitle:"Streaming destinations",destSub:"Enter your channel details",ytLabel:"YouTube Stream Key",ytPlaceholder:"Enter your YouTube Stream Key",fbLabel:"Facebook Stream Key",fbPlaceholder:"Enter your Facebook Stream Key",show:"Show",hide:"Hide",
keyNotice:"Keep Stream Keys secret like passwords. They are stored only in this mobile browser storage; never share them.",saveSettings:"Save settings",
liveTitle:"Live broadcast",liveSub:"Prepare your live broadcast",engineStatus:"Streaming engine is not connected",engineDetail:"This is a control panel; an RTMP relay/server is required.",startLive:"● Start YouTube + Facebook Live",liveNotice:"The Start button is disabled because this package does not include an RTMP video engine or cloud server. Entering Stream Keys alone will not start a live stream.",
installTitle:"Install on mobile",install1:"Upload this PWA to HTTPS web hosting.",install2:"Open the website in Android Chrome.",install3:"From the ⋮ menu, choose Add to Home screen or Install app.",footer:"AI Cricket Streamer · Mobile PWA Prototype\nBuilt with free open web technologies",
saved:"Settings saved on this mobile device",badUrl:"Please enter a valid web URL"
}};
let lang=localStorage.getItem("aics-language")||"ur";
function applyLanguage(){
 const t=words[lang]||words.ur;document.documentElement.lang=lang;document.documentElement.dir=lang==="ur"?"rtl":"ltr";
 document.querySelectorAll("[data-i18n]").forEach(el=>{const k=el.dataset.i18n;if(t[k]!==undefined)el.textContent=t[k];});
 document.querySelectorAll("[data-i18n-placeholder]").forEach(el=>{const k=el.dataset.i18nPlaceholder;if(t[k]!==undefined)el.placeholder=t[k];});
 $("langUr").classList.toggle("lang-active",lang==="ur");$("langEn").classList.toggle("lang-active",lang==="en");
 document.querySelectorAll("[data-toggle]").forEach(b=>{const input=$(b.dataset.toggle);b.textContent=input.type==="password"?t.show:t.hide;});
}
$("langUr").onclick=()=>{lang="ur";localStorage.setItem("aics-language",lang);applyLanguage();};
$("langEn").onclick=()=>{lang="en";localStorage.setItem("aics-language",lang);applyLanguage();};
try{const saved=JSON.parse(localStorage.getItem("aics-settings")||"{}");$("scoreUrl").value=saved.scoreUrl||defaults.scoreUrl;$("ytKey").value=saved.ytKey||"";$("fbKey").value=saved.fbKey||"";}catch(e){}
const toast=m=>{const t=$("toast");t.textContent=m;t.classList.add("show");setTimeout(()=>t.classList.remove("show"),2300)};
function save(){const data={scoreUrl:$("scoreUrl").value.trim(),ytKey:$("ytKey").value,fbKey:$("fbKey").value};localStorage.setItem("aics-settings",JSON.stringify(data));toast(words[lang].saved);}
$("saveUrl").onclick=save;$("saveKeys").onclick=save;
$("openScore").onclick=()=>{const u=$("scoreUrl").value.trim();try{const parsed=new URL(u);if(!["http:","https:"].includes(parsed.protocol))throw 0;save();window.open(parsed.href,"_blank","noopener");}catch(e){toast(words[lang].badUrl);}};
document.querySelectorAll("[data-toggle]").forEach(b=>b.onclick=()=>{const i=$(b.dataset.toggle);i.type=i.type==="password"?"text":"password";b.textContent=i.type==="password"?words[lang].show:words[lang].hide;});
applyLanguage();
if("serviceWorker" in navigator && location.protocol.startsWith("http"))navigator.serviceWorker.register("./sw.js").catch(()=>{});
