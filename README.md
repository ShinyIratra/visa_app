# Tanjona
Miscan code QR @ phone dia mankany @ frontoffice ny navigateur an le phone.

Izany hoe mila **public ilay URL an'ilay frontoffice**, izany hoe :
- soit deployena ilay izy 
- soit mampiasa ngrok (url public iray ihany) na cloudflared (url public izay tiana)

> Dia zah tsy tompon'ny repo an lisany roa (front, back) dia cloudflared no nosafidiako.

--- 

Dia rehefa tonga ao @ frontoffice le navigateur an le phone dia le code REACT ao mitifitra an'i springboot (normalement `localhost:8080`).

Blem: mila **public koa ny URL backoffice** manjary localhost an le phone indray no tifirin le navigateur.

> Dia nampiasa cloudflared koa zah ho an le backoffice (naika nanao ngrok fa aleo tsy maka alavitra)

---

# Setup cloudflared:
- Telechargement: https://developers.cloudflare.com/tunnel/downloads/, raha windows 64 bit ito misy lient direct: https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe
- Commande manome an le url public:
```bash
# Terminal 1 - Spring Boot
cloudflared tunnel --url http://localhost:8080

# Terminal 2 - Vite/React
cloudflared tunnel --url http://localhost:5173
```

- Dia izay url public omeny anareo ao (du genre:  https://blabla.trycloudflare.com) no atao anaty properties:
    - `app.frontoffice-base-url` ny key ho an'ny `application.properties`
    - `VITE_BACKOFFICE_BASE_URL` ny key ho an'ny `.env`

---

# Note:
nasiko prefix ny app roa:
- Springboot: `/backoffice` 
- React: `/frontoffice`

amzay tsy mila miconfig an reo intsony any aoriana any au cas ou