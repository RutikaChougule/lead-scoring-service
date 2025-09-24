# 🚀 Lead Scoring Service

**Automate your lead qualification with AI + rule-based scoring!**
This Spring Boot service takes your CSV of leads, evaluates them against your offer, and gives you a **score + intent label** (High / Medium / Low) — all automatically.

---

![Java](https://img.shields.io/badge/Java-21-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-green?logo=springboot)
![OpenAI](https://img.shields.io/badge/OpenAI-API-purple)

---

## ✨ Features

* ✅ Upload CSV leads (name, role, company, industry, location, LinkedIn bio)
* ✅ Define your offer with value props and ideal use cases
* ✅ **Rule-based scoring** (role relevance, industry match, data completeness)
* ✅ **AI-based scoring** using OpenAI API
* ✅ Calculate **final score (0–100)** and assign **intent labels**
* ✅ Export results as **CSV** for analysis

---

## 💡 Why This Service?

Manually qualifying leads is time-consuming and error-prone.
This service combines **human-like AI intuition** with **hard business rules** to give you **fast, reliable insights** into which leads are worth your time.

---

## 🛠 Tech Stack

* **Java 21** + Spring Boot 3
* **WebFlux** for async AI API calls
* **CSV parsing** via `opencsv` or `commons-csv`
* **Maven** for dependency management

---

## 🔑 Environment Variables

> ⚠️ Never commit your API key to GitHub

| Variable         | Description                                  |
| ---------------- | -------------------------------------------- |
| `OPENAI_API_KEY` | Your OpenAI API key                          |
| `OPENAI_API_URL` | `https://api.openai.com/v1/chat/completions` |

---

## 🚀 Getting Started

### 1️⃣ Clone Repo

```bash
git clone https://github.com/<your-username>/lead-scoring-service.git
cd lead-scoring-service
```

### 2️⃣ Set Environment Variables

**Mac/Linux:**

```bash
export OPENAI_API_KEY="your_openai_key"
export OPENAI_API_URL="https://api.openai.com/v1/chat/completions"
```

**Windows (cmd):**

```cmd
setx OPENAI_API_KEY "your_openai_key"
setx OPENAI_API_URL "https://api.openai.com/v1/chat/completions"
```

### 3️⃣ Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

Your service will start on `http://localhost:8080`.

---

## 📝 API Endpoints

### 1. Save Offer

```http
POST /offer
Content-Type: application/json
```

**Body Example:**

```json
{
  "name": "Awesome SaaS Tool",
  "valueProps": "Boost productivity by 30%",
  "idealUseCases": "SaaS mid-market companies"
}
```

---

### 2. Upload Leads

```http
POST /leads/upload
Content-Type: multipart/form-data
```

CSV must include:
`name,role,company,industry,location,linkedinBio`

---

### 3. Score Leads

```http
POST /score
```

**Response Example:**

```json
[
  {
    "name": "Ava Patel",
    "role": "Head of Growth",
    "company": "FlowMetrics",
    "intent": "High",
    "score": 85,
    "reasoning": "Rule: 35 + AI: 50"
  }
]
```

---

### 4. Export Results as CSV

```http
GET /results/export
```

Returns CSV content ready for download.

---

## 🚀 Deployment on Railway

1. Create a new Railway project
2. Add **Environment Variables**:

   * `OPENAI_API_KEY`
   * `OPENAI_API_URL`
3. Connect your GitHub repo
4. Deploy — Railway automatically builds and runs your service

---

## ⚡ Pro Tips

* For **large CSVs**, AI scoring may take longer — consider batching.
* Use this service to **pre-qualify leads** and save your sales team hours every week.

---

## 📂 License

MIT License
