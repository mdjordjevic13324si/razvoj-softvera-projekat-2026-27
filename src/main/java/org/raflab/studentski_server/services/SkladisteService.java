package org.raflab.studentski_server.services;

import org.raflab.studentski_server.exceptions.BadRequestException;
import org.raflab.studentski_server.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/*
 * Cuvanje fajlova (zip zadataka, kasnije i resenja) na disku.
 * Svi fajlovi su ispod jednog korenskog foldera (app.skladiste.koren),
 * a u bazi se cuva samo relativna putanja, npr. 2026-2027/januar/RS/zadaci/termin-1/grupa-A.zip
 */
@Service
public class SkladisteService {

    private final Path koren;

    public SkladisteService(@Value("${app.skladiste.koren}") String koren) {
        this.koren = Path.of(koren).toAbsolutePath().normalize();
    }

    // snima fajl na zadatu relativnu putanju (pravi foldere, postojeci fajl zamenjuje)
    public void sacuvaj(String relativnaPutanja, MultipartFile fajl) {
        Path odrediste = punaPutanja(relativnaPutanja);
        try (InputStream sadrzaj = fajl.getInputStream()) {
            Files.createDirectories(odrediste.getParent());
            Files.copy(sadrzaj, odrediste, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Fajl nije sacuvan: " + relativnaPutanja, e);
        }
    }

    // vraca fajl sa zadate relativne putanje; 404 ako ga nema na disku
    public Resource ucitaj(String relativnaPutanja) {
        Path putanja = punaPutanja(relativnaPutanja);
        if (!Files.isRegularFile(putanja)) {
            throw new ResourceNotFoundException("Fajl ne postoji u skladistu: " + relativnaPutanja);
        }
        return new FileSystemResource(putanja);
    }

    // zip mora da ima ime koje se zavrsava na .zip i da pocinje bajtovima "PK" (potpis zip formata)
    public void proveriDaJeZip(MultipartFile fajl) {
        if (fajl == null || fajl.isEmpty()) {
            throw new BadRequestException("Fajl je prazan");
        }
        String ime = fajl.getOriginalFilename();
        if (ime == null || !ime.toLowerCase().endsWith(".zip")) {
            throw new BadRequestException("Fajl mora biti .zip");
        }
        try (InputStream sadrzaj = fajl.getInputStream()) {
            byte[] pocetak = sadrzaj.readNBytes(2);
            if (pocetak.length < 2 || pocetak[0] != 'P' || pocetak[1] != 'K') {
                throw new BadRequestException("Fajl nije ispravan zip");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Fajl ne moze da se procita", e);
        }
    }

    // putanja ne sme da izadje iz korenskog foldera (npr. preko "..")
    private Path punaPutanja(String relativnaPutanja) {
        Path putanja = koren.resolve(relativnaPutanja).normalize();
        if (!putanja.startsWith(koren)) {
            throw new BadRequestException("Neispravna putanja fajla");
        }
        return putanja;
    }
}
