# Share : Asset Manager Framework

**Share** è un framework professionale per la gestione e la prenotazione di risorse condivise (asset) all'interno di gruppi di lavoro. Il sistema automatizza l'intero ciclo di vita di un bene, garantendo la tracciabilità delle responsabilità attraverso report dinamici e integrazioni con sistemi di audit esterni.

---

## 👥 Attori e Capability

Il sistema implementa una separazione netta delle responsabilità per garantire la sicurezza e l'efficienza operativa:

| Attore | Descrizione e Responsabilità |
| :--- | :--- |
| **Administrator** | Crea i gruppi operativi, gestisce gli inviti tramite **Token/Link**, definisce le tipologie di asset e i relativi report di riconsegna. Ha il potere di bloccare asset o utenti in tempo reale. |
| **Operator** | Accede al gruppo tramite invito, prenota gli asset in fasce orarie disponibili e ha l'obbligo di compilare il report tecnico al termine dell'utilizzo. |
| **Technician** | Riceve notifiche automatiche in caso di guasto, consulta la lista degli interventi ordinata per priorità e ripristina la disponibilità degli asset. |

---

## 🚀 Funzionalità Principali

### 1. Gestione Dinamica degli Asset
L'Administrator può configurare ogni categoria di asset definendo:
* **Custom Report**: Domande specifiche sullo stato del bene (Esito: *Buono* o *Guasto*).
* **Livello di Urgenza**: Scala di importanza da 1 a 3 per i guasti segnalati.
* **Vincoli Temporali**: Definizione della durata della giornata lavorativa e dell'intervallo massimo di utilizzo giornaliero per ogni tipologia di asset.

### 2. Motore di Prenotazione "Conflict-Free"
* **Pianificazione Anticipata**: Gli Operator possono prenotare asset fino a **7 giorni** prima della data corrente.
* **Fasce Orarie Intelligenti**: In fase di prenotazione, il sistema mostra esclusivamente le fasce orarie realmente disponibili, calcolate sottraendo le prenotazioni esistenti e i periodi di inattività (stati *Guasto* o *Bloccato*).

### 3. Workflow e Lifecycle (State Pattern)
La logica di business è governata da una macchina a stati finiti che impedisce operazioni non autorizzate:
* **AVAILABLE**: Asset libero e prenotabile.
* **IN_USE**: Asset attualmente in consegna all'Operator.
* **FAULTY**: Asset rimosso dalla disponibilità a seguito di un report negativo, in attesa del Technician.
* **BLOCKED**: Asset disabilitato manualmente dall'Administrator.

---

## 🔌 Integrazioni con Sistemi Esterni

Il framework è progettato per comunicare con entità esterne al fine di certificare i processi:

1. **E1 - Audit & Compliance System**: Ad ogni segnalazione di guasto, il sistema genera automaticamente un **documento PDF** contenente i dettagli del danno, l'identità dell'Operator, il timestamp e il Technician assegnato.
2. **E2 - Analytics Engine**: Il sistema invia dati aggregati sull'utilizzo e sui tempi di fermo (downtime) per monitorare l'affidabilità degli asset nel tempo.

---
