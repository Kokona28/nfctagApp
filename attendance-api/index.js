const express = require("express");
const cors = require("cors");

const sqlite3 = require("sqlite3").verbose();
const db = new sqlite3.Database("./attendance.db");

const app = express();
app.use(cors());
app.use(express.json());

db.serialize(() => {
    db.run(`
        CREATE TABLE IF NOT EXISTS attendance (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            userId TEXT,
            type TEXT,
            time TEXT
        )
    `);
});

// 出勤・退勤登録API
app.post("/attendance", (req, res) => {
    const { userId, type, time } = req.body;

    const record = {
        userId,
        type,
        time
    };

    db.run(
        "INSERT INTO attendance (userId, type, time) VALUES (?, ?, ?)",
        [userId, type, time],
        (err) => {
            if (err) {
                console.error(err);
                return res.status(500).json({ status: "error" });
            }
            console.log("受信:", record);
            res.json({ status: "success" });
        }
    );
});

// 履歴取得API
app.get("/attendance", (req, res) => {
    db.all("SELECT * FROM attendance", (err, rows) => {
        if (err) {
            console.error(err);
            return res.status(500).json({ status: "error" });
        }
        res.json(rows);
    });
});

// サーバ起動
app.listen(3000, () => {
    console.log("サーバ起動：http://localhost:3000");
});