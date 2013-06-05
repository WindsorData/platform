zauber = Company.create(name: "Zauber", address: "Costa Rica 5546, Palermo, Bs As")
Company.create(name: "Rojo Oriente", address: "Uriarte 1284, Palermo, Bs As")

Ticker.create([{name: "aapl"},{name: "fb"}, {name: "goog"}])

User.create(email: "super@windsor.com", password: "123456", password_confirmation: "123456", role: "super")
User.create(email: "admin@windsor.com", password: "123456", password_confirmation: "123456", role: "admin")
User.create(email: "client@windsor.com", password: "123456", password_confirmation: "123456", role: "client", company: zauber)
