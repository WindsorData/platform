zauber = Company.create(name: "Zauber", address: "Costa Rica 5546, Palermo, Bs As")
Company.create(name: "Rojo Oriente", address: "Uriarte 1284, Palermo, Bs As")

# Users
User.create(email: "super@windsor.com", password: "123456", password_confirmation: "123456", role: "super")
User.create(email: "admin@windsor.com", password: "123456", password_confirmation: "123456", role: "admin")
client_from_zauber = User.create(email: "client@windsor.com", password: "123456", password_confirmation: "123456", role: "client", company: zauber)

# Tickers
ticker_aapl = Ticker.create(name: "AAPL")
ticker_fb = Ticker.create(name: "FB")

# Groups
group1 = Group.new(name: 'group with company', company: client_from_zauber.company)
group1.tickers << ticker_aapl
group1.save()

group2 =  Group.new(name: 'group without company')
group1.tickers << ticker_aapl
group1.tickers << ticker_fb
group2.save()

Role.create([{name: "CTO"},{name: "CFO"}])