# Users
User.destroy_all
User.create!(email: "super@windsor.com", password: "123123", password_confirmation: "123123", role: "super")
User.create!(email: "admin@windsor.com", password: "123123", password_confirmation: "123123", role: "admin")
User.create!(email: "client@windsor.com", password: "123123", password_confirmation: "123123", role: "client")
User.create!(email: "peer@windsor.com", password: "123123", password_confirmation: "123123", role: "peer")

