# TrendAI

TrendAI, Spring Boot tabanlı bir e-ticaret backend uygulamasıdır.

## Gereksinimler

- Java 21
- Maven
- PostgreSQL
- PostgreSQL üzerinde \trendai` veritabanı`

## Environment Variables

Uygulamayı çalıştırmak için aşağıdaki environment variable'lar tanımlanmalıdır:

``` 
DB_URL=jdbc:postgresql://localhost:5432/trendai
DB_USERNAME=postgres
DB_PASSWORD=your_password

AI_ENABLED=false
AI_DEMO_MODE=false

OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=your_openai_model
``` 

## Uygulamayı Çalıştırma

Öncelikle PostgreSQL'in çalıştığından ve `\trendai` veritabanının oluşturulduğundan emin olun.

Testleri çalıştırmak için:
``` 
.\mvnw.cmd test
``` 
Uygulamayı çalıştırmak için:
``` 
.\mvnw.cmd spring-boot:run
``` 
Uygulama varsayılan olarak `\http://localhost:8080\` adresinde çalışır.

## AI Demo Mode

OpenAI API kredisi bulunmadığında AI özelliklerini göstermek için Demo Mode kullanılabilir.

Environment variable'lar:
``` 
AI_ENABLED=true
AI_DEMO_MODE=true
``` 
Demo Mode aktifken OpenAI'a istek gönderilmez.

Demo Mode gerçek bir LLM değildir. Desteklenen demo sorgularında mevcut veritabanındaki gerçek ürünleri kullanır ve ProductSearchTool ile ProductDetailsTool üzerinden ürün bilgilerini getirir.

Demo Mode response'larında mode bilgisi:
```
{
"answer": "...",
"mode": "DEMO"
}
```