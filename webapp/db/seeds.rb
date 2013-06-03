# Client User
User.create(email: "user@windsor.com", password: "123456", password_confirmation: "123456", role: "client")

# Admin User
User.create(email: "admin@windsor.com", password: "123456", password_confirmation: "123456", role: "admin")

# Super User
User.create(email: "super@windsor.com", password: "123456", password_confirmation: "123456", role: "super")


Ticker.create([{name: "aapl"},{name: "fb"}, {name: "goog"}])

Company.create([{name: "Zauber", address: "Costa Rica 5546, Palermo, Bs As"}, {name: "Rojo Oriente", address: "Uriarte 1284, Palermo, Bs As"}])