zauber = Company.create!(name: "Zauber", address: "Costa Rica 5546, Palermo, Bs As", contact_email: "contact@zauber.com")
Company.create!(name: "Rojo Oriente", address: "Uriarte 1284, Palermo, Bs As")

# Users
User.create!(email: "super@windsor.com", password: "123456", password_confirmation: "123456", role: "super")
User.create!(email: "admin@windsor.com", password: "123456", password_confirmation: "123456", role: "admin")
client_from_zauber = User.create!(email: "client@windsor.com", password: "123456", password_confirmation: "123456", role: "client", company: zauber)

# Tickers
ticker_aapl = Ticker.create!(ticker: 'appl', name: 'Apple Inc')
ticker_fb = Ticker.create!(ticker: 'fb', name: 'Facebook Inc')

# Groups
group1 = Group.new(name: 'group with company', company: client_from_zauber.company)
group1.tickers << ticker_aapl
group1.save!

group2 =  Group.new(name: 'group without company')
group2.tickers << ticker_aapl
group2.tickers << ticker_fb
group2.save!

PrimaryRole.create!([{name: "CTO"},{name: "CFO"}])