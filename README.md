# Doctorin E2E Testleri

Bu proje, Doctorin platformu için **Selenium, Java, Cucumber (BDD)** teknolojileriyle hazırlanmış **uçtan uca (E2E)** kullanıcı arayüzü testlerini içerir. Amaç randevu süreçlerinin (oluşturma, check-in ve silme) ile çalışma takvimi akışlarının gerçek kullanıcı davranışına yakın adımlarla güvenilir biçimde doğrulanmasıdır.

### Öne Çıkanlar
- BDD ile okunabilir ve iş birimi terimlerine uygun senaryolar
- Oynak/kararsız arayüz durumlarına dayanıklı bekleme ve etkileşim yardımcıları
- Yeniden kullanılabilir sayfa nesneleri (Page Object) ve adım tanımları
- Allure ile zengin raporlama ve hata anında ekran görüntüsü ekleri

---

## Proje Structure

```text
doctorin-e2e-tests/
├─ pom.xml                                  # Maven projesi; kütüphaneler ve eklentiler
├─ src
│  ├─ main/java/com/sinannuhoglu
│  │  ├─ core/
│  │  │  ├─ BasePage.java                   # Ortak bekleme/etkileşim; güvenli click/jsClick/scroll
│  │  │  ├─ ConfigReader.java               # Çok-kaynaklı config; placeholder çözümü; süre/parsing yardımcıları
│  │  │  ├─ DriverFactory.java              # Lokal veya Grid WebDriver üretimi; seçenekler
│  │  │  ├─ DriverManager.java              # ThreadLocal WebDriver; paralel çalışmaya uygun temel
│  │  │  ├─ PageFactory.java                # (WebDriver, ConfigReader) imzalarını öncelikli deneyen üretici
│  │  │  └─ TestContext.java                # Yaşam döngüsü (init/quit); sistem özelliklerini yayma
│  │  ├─ pages/
│  │  │  ├─ LoginPage.java                  # Login sayfası/iframe farkındalığı; submit stratejisi
│  │  │  ├─ DashboardPage.java              # Ana panel; modül geçişleri
│  │  │  ├─ AppointmentsPage.java           # Filtre/slot/hasta arama/kaydet/check-in/silme
│  │  │  ├─ AppointmentDefinitionsPage.java # Definitions > Resources gezinme & grid yardımcıları
│  │  │  ├─ AppointmentResourceWorkplanPage.java # Workplan modalını açma ve gün seçimi
│  │  │  ├─ AppointmentWorkplanBarPage.java # Workplan bar alan etkileşimleri (branch, type, dept, saat)
│  │  │  ├─ TenantSelectPage.java           # Tenant değiştirme modalı
│  │  │  └─ ResourceEditorDialogPage.java   # (destekleyici kısa etkileşimler)
│  │  └─ util/
│  │     └─ AppConfig.java                  # baseUrl çözümleme (system -> config önceliği)
│  └─ test/java/com/sinannuhoglu
│     ├─ hooks/
│     │  └─ Hooks.java                      # @Before init; @After hata ekran görüntüsü + teardown
│     ├─ runners/
│     │  └─ SmokeRunner.java                # TestNG+Cucumber koşumcusu; Allure plugin
│     └─ steps/                             # Step Definitions
│        ├─ LoginSteps.java                 # Login/Tenant
│        ├─ DashboardSteps.java             # Modül açılışları
│        ├─ AppointmentFilterSteps.java     # Şube/Departman/Doktor filtreleri
│        ├─ AppointmentSlotSteps.java       # Slot, hasta arama, kaydet, check-in, silme
│        ├─ AppointmentsSteps.java          # Sayfa doğrulamaları
│        ├─ AppointmentDefinitionsSteps.java# Definitions/Resources gezinme
│        └─ AppointmentResourceWorkplanSteps.java # Workplan işlemleri
└─ src/test/resources
   ├─ config/                                # default/dev/staging/prod/test .properties
   └─ features/
      ├─ login/login_success.feature
      └─ appointment/
         ├─ appointment_filter.feature
         ├─ appointment_e2e.feature
         ├─ appointment_resources.feature
         └─ workplan_to_appointment_e2e.feature
```

---

# Technologies Used and Their Purpose

| Teknoloji / Araç                 | Sürüm    | Amaç / Kullanım                                                                 |
|----------------------------------|---------:|----------------------------------------------------------------------------------|
| **Java (JDK)**                   | 21       | Test otomasyonu dili                                                             |
| **Selenium**                     | 4.26.0   | Web arayüz otomasyonu; bekleme ve etkileşim API’leri                                  |
| **Cucumber (Java/TestNG/Pico)**  | 7.17.0   | BDD çerçevesi; Feature dosyaları ile Step tanımlarının eşleştirilmesi                                          |
| **TestNG**                       | 7.10.2   | Test koşumu, paralel çalıştırma ve doğrulama altyapısı                           |
| **WebDriverManager**             | 5.9.2    | Yerel sürücülerin otomatik yönetimi                                             |
| **Allure Framework**             | 2.29.0   | Zengin test raporları; adım akışı ve ekler                                      |
| **Allure Maven Plugin**          | 2.12.0   | Rapor üretimi ve servis komutları                                               |
| **Maven Surefire Plugin**        | 3.2.5    | Maven üzerinden testlerin çalıştırılması                                           |
| **SLF4J (simple)**               | 2.0.13   | Basit log çıktıları                                                               |

---

## Test Coverage

**1. Kimlik Doğrulama & Kurum Seçimi**: Giriş sayfasının yüklenmesi (iframe duyarlılığı), kiracı seçimi ve formun kaydedilmesi, geçerli kullanıcıyla oturum açılması ve kontrol paneline (dashboard) yönlendirme.

**2. Randevular – Filtreler**: Filtre panelinin açılması ve uygulanması; Şube, Departman, Doktor alanları arasındaki bağımlılıkların doğrulanması; çoklu doktor seçiminde yalnızca hedef doktorun bırakılması.

**3. Randevu Yaşam Döngüsü**: Slot tıklama (dakika 00 ile 30 sıralamasının dinamik belirlenmesi), hasta arama ve seçme, yan panelde kaydetme, hızlı bilgi penceresinde check-in durumunun görülmesi, silme onayı ve gerekliyse gerekçe diyalogunun kapatılması, ilgili kartın ekranda yer almamasının doğrulanması.

**4. Çalışma Takvimi**: Kaynağı düzenleme diyalogunun açılması, Çalışma Takvimi sekmesine geçiş, gün ve saat aralıklarının girilmesi, Şube, Randevu Tipi, Platform ve Departman seçimleri, değişikliklerin kaydedilmesi ve Randevular sayfasına dönüşün doğrulanması.

