package tokoibuelin.storesystem.service;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokoibuelin.storesystem.entity.Address;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.request.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.UserDto;
import tokoibuelin.storesystem.repository.AddressRepository;
import tokoibuelin.storesystem.repository.UserRepository;
import tokoibuelin.storesystem.util.HexUtils;
import tokoibuelin.storesystem.util.JwtUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import tokoibuelin.storesystem.util.Base64Utils;


@Service
public class UserService extends AbstractService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final byte[] jwtKey;

    public UserService(final Environment env, final UserRepository userRepository, final PasswordEncoder passwordEncoder, final AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        final String skJwtKey = env.getProperty("jwt.secret-key");
        this.jwtKey = HexUtils.hexToBytes(skJwtKey);
        this.addressRepository = addressRepository;
    }

    public Response<Object> login(final LoginReq req) {
        if (req == null) {
            return Response.badRequest();
        }
        final Optional<User> userOpt = userRepository.findByEmail(req.email());
        if (userOpt.isEmpty()) {
            return Response.create("08", "01", "Email  salah", null);
        }
        final User user = userOpt.get();

        String encodedPassword = Base64Utils.base64Encode(req.password().getBytes());

        if (!passwordEncoder.matches(req.password(), user.password())) {
            return Response.create("08", "02", "password salah", null);
        }
        final Authentication authentication = new Authentication(user.userId(), user.role(), true);
        //SecurityContextHolder.setAuthentication(authentication);
        final long iat = System.currentTimeMillis();
        final long exp = 1000 * 60 * 60 * 24; // 24 hour
        final JwtUtils.Header header = new JwtUtils.Header() //
                .add("typ", "JWT") //
                .add("alg", "HS256"); //
        final JwtUtils.Payload payload = new JwtUtils.Payload() //
                .add("sub", authentication.id()) //
                .add("role", user.role().name()) //
                .add("iat", iat) //
                .add("exp", exp); //
        final String token = JwtUtils.hs256Tokenize(header, payload, jwtKey);
        return Response.create("08", "00", "Sukses", token);
    }

    @Transactional
    public Response<Object> registerSupplier(final Authentication authentication, final RegistSupplierReq req) {
        // Periksa precondition
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {

            // Validasi request
            if (req == null) {
                return Response.badRequest();
            }

            // Encode password
            final String encoded = passwordEncoder.encode(req.password());

            // Buat objek User
            final User user = new User(
                    null, // ID akan dihasilkan setelah penyimpanan
                    req.name(),
                    req.email(),
                    encoded,
                    User.Role.PEMASOK,
                    req.phone(),
                    null, // Alamat akan disimpan setelah user disimpan
                    authentication.id(),
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null
            );

            // Simpan user
            final String savedId = userRepository.saveUser(user);

            // Jika user berhasil disimpan, simpan alamat
            if (savedId != null) {
                Address address = new Address(
                        null, // ID alamat akan dihasilkan setelah penyimpanan
                        savedId, // ID user yang baru disimpan
                        req.street(),
                        req.rt(),
                        req.rw(),
                        req.village(),
                        req.district(),
                        req.city(),
                        req.postalCode()
                );

                // Simpan alamat
                String addressSavedId = addressRepository.saveAddress(address);

                // Jika alamat berhasil disimpan
                if (addressSavedId != null) {
                    return Response.create("05", "00", "Sukses", savedId);
                }
            }

            // Jika ada yang gagal
            return Response.create("05", "01", "Gagal mendaftarkan supplier", null);
        });
    }

    public Response<Object> registerPegawai(final Authentication authentication, final RegistSupplierReq req) {
        // Periksa precondition
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {

            // Validasi request
            if (req == null) {
                return Response.badRequest();
            }

            // Encode password
            final String encoded = passwordEncoder.encode(req.password());

            // Buat objek User
            final User user = new User(
                    null, // ID akan dihasilkan setelah penyimpanan
                    req.name(),
                    req.email(),
                    encoded,
                    User.Role.PEGAWAI,
                    req.phone(),
                    null, // Alamat akan disimpan setelah user disimpan
                    authentication.id(),
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null
            );

            // Simpan user
            final String savedId = userRepository.saveUser(user);

            // Jika user berhasil disimpan, simpan alamat
            if (savedId != null) {
                Address address = new Address(
                        null, // ID alamat akan dihasilkan setelah penyimpanan
                        savedId, // ID user yang baru disimpan
                        req.street(),
                        req.rt(),
                        req.rw(),
                        req.village(),
                        req.district(),
                        req.city(),
                        req.postalCode()
                );

                // Simpan alamat
                String addressSavedId = addressRepository.saveAddress(address);

                // Jika alamat berhasil disimpan
                if (addressSavedId != null) {
                    return Response.create("05", "00", "Sukses", savedId);
                }
            }

            // Jika ada yang gagal
            return Response.create("05", "01", "Gagal mendaftarkan supplier", null);
        });
    }

    @Transactional
    public Response<Object> registerBuyer( final RegisterReq req) {
            if (req == null) {
                return Response.badRequest();
            }

            final String encoded = passwordEncoder.encode(req.password());
            final User user = new User(
                    null,
                    req.name(),
                    req.email(),
                    encoded, 
                    User.Role.PELANGGAN,
                    req.phone(),
                    null,
                    null, 
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null
            );

            final String savedId = userRepository.saveUser(user); // mengubah dari Long menjadi String

            if (savedId != null) {
            // Lakukan update pada kolom created_by dengan ID yang baru didapat
            boolean updated = userRepository.updateUserCreatedBy(savedId, savedId);

            if (updated) {
                // Jika update berhasil, kembalikan response sukses
                return Response.create("05", "00", "Sukses", savedId);
            } else {
                // Jika update gagal, lempar RuntimeException agar transaksi di-rollback
                throw new RuntimeException("Gagal memperbarui kolom 'created_by' untuk user dengan ID: " + savedId);
                // Atau, Anda bisa mengembalikan Response.create("05", "01", "Gagal memperbarui created_by User", null);
                // Tergantung pada bagaimana Anda ingin menangani kegagalan update ini.
            }
        }

        // Jika savedId null (userRepository.saveUser gagal)
        return Response.create("05", "01", "Gagal mendaftarkan sebagai User", null);

    }

    public Response<Object> resetPassword(final ResetPasswordReq req) {
        try {
            if (req.newPassword() == null || req.newPassword().isEmpty()) {
                return Response.create("07", "06", "Password baru tidak boleh kosong", null);
            }
            if (req.newPassword().length() < 8) {
                return Response.create("07", "05", "Password baru harus memiliki minimal 8 karakter", null);
            }

            Optional<User> userOpt = userRepository.findByEmail(req.email());
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "Pengguna tidak ditemukan", null);
            }

            User user = userOpt.get();
            if (passwordEncoder.matches(req.newPassword(), user.password())) {
                return Response.create("07", "02", "Password lama dan password baru sama. Silakan gunakan password yang berbeda",
                        null);
            }

            if (user.deletedBy() != null  || user.deletedAt() != null) {
                return Response.create("07", "03", "Akun pengguna telah dihapus", null);
            }

            String newEncodedPassword = passwordEncoder.encode(req.newPassword());
            String updatedUserId = userRepository.resetPassword(user.userId(), newEncodedPassword);
            if (updatedUserId == null) {
                return Response.create("07", "04", "Gagal memperbarui password", null);
            }

            UserDto userDto = new UserDto(user.userId(), user.name());
            return Response.create("07", "00", "Password berhasil diperbarui", userDto);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.create("07", "07", "Terjadi kesalahan", e.getMessage());
        }
    }

    public Response<Object> deletedUser(Authentication authentication, String userId) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<User> userOpt = userRepository.findById(userId);

            if (!userOpt.isPresent()) {
                return Response.create("10", "02", "ID tidak ditemukan", null);
            }

            User dataUser = userOpt.get();

            if (dataUser.deletedAt() != null) {
                return Response.create("10", "03", "Data sudah dihapus", null);
            }

            User updatedUser = new User(
                    dataUser.userId(),
                    dataUser.name(),
                    dataUser.email(),
                    dataUser.password(),
                    dataUser.role(),
                    dataUser.phone(),
                    dataUser.address(),
                    dataUser.createdBy(),
                    dataUser.updatedBy(),
                    authentication.id(),
                    dataUser.createdAt(),
                    dataUser.updatedAt(),
                    OffsetDateTime.now());

            Long updatedRows = userRepository.deletedUser(updatedUser);
            if (updatedRows > 0) {
                return Response.create("10", "00", "Berhasil hapus data", null);
            } else {
                return Response.create("10", "01", "Gagal hapus data", null);
            }
        });
    }

    @Transactional
    public Response<Object> updateProfile(final Authentication authentication, final UpdateProfileReq req) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PELANGGAN, User.Role.PEMASOK).orElseGet(() -> {
            // Ambil pengguna berdasarkan ID
            Optional<User> userOpt = userRepository.findById(authentication.id());
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "User tidak ditemukan", null);
            }

            User user = userOpt.get();

            // Perbarui informasi pengguna
            User updatedUser = new User(
                    user.userId(),
                    req.name() != null ? req.name() : user.name(),
                    req.email() != null ? req.email() : user.email(),
                    user.password(), // Password tidak diubah
                    user.role(),
                    req.phone() != null ? req.phone() : user.phone(),
                    user.address(), // Alamat mungkin perlu diperbarui terpisah
                    user.createdBy(),
                    authentication.id(),
                    user.deletedBy(),
                    user.createdAt(),
                    OffsetDateTime.now(),
                    user.deletedAt()
            );
            // Perbarui pengguna di database
            if (userRepository.updateUser(updatedUser)) {
                // Jika alamat baru disediakan, perbarui alamat
                if (req.street() != null || req.rt() != null || req.rw() != null || req.village() != null || req.district() != null || req.city() != null || req.postalCode() != null) {
                    Address updatedAddress = new Address(
                            user.address() != null ? user.address().addressId() : null, // Gunakan ID alamat yang sudah ada
                            updatedUser.userId(), // ID user baru
                            req.street() != null ? req.street() : (user.address() != null ? user.address().street() : null),
                            req.rt() != null ? req.rt() : (user.address() != null ? user.address().rt() : null),
                            req.rw() != null ? req.rw() : (user.address() != null ? user.address().rw() : null),
                            req.village() != null ? req.village() : (user.address() != null ? user.address().village() : null),
                            req.district() != null ? req.district() : (user.address() != null ? user.address().district() : null),
                            req.city() != null ? req.city() : (user.address() != null ? user.address().city() : null),
                            req.postalCode() != null ? req.postalCode() : (user.address() != null ? user.address().postalCode() : null)
                    );

                    // Perbarui alamat di database
                    if (addressRepository.updateAddress(updatedAddress)) {
                        return Response.create("07", "00", "Profil berhasil diupdate", null);
                    } else {
                        return Response.create("07", "03", "Gagal mengupdate alamat", null);
                    }
                }
                return Response.create("07", "00", "Profil berhasil diupdate", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate profil", null);
            }
        });
    }

    @Transactional
    public Response<Object> updateSupplier(final Authentication authentication, final UpdateProfileReq req, final String supplierId) {
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {
            // Ambil pengguna berdasarkan ID
            Optional<User> userOpt = userRepository.findById(supplierId);
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "User tidak ditemukan", null);
            }

            User user = userOpt.get();

            // Perbarui informasi pengguna
            User updatedUser = new User(
                    user.userId(),
                    req.name() != null ? req.name() : user.name(),
                    req.email() != null ? req.email() : user.email(),
                    user.password(), // Password tidak diubah
                    user.role(),
                    req.phone() != null ? req.phone() : user.phone(),
                    user.address(), // Alamat mungkin perlu diperbarui terpisah
                    user.createdBy(),
                    authentication.id(),
                    user.deletedBy(),
                    user.createdAt(),
                    OffsetDateTime.now(),
                    user.deletedAt()
            );

            boolean userUpdateSuccess = userRepository.updateUser(updatedUser);
            // Perbarui pengguna di database
            if (userUpdateSuccess) {
                // Jika alamat baru disediakan, perbarui alamat
                if (req.street() != null || req.rt() != null || req.rw() != null || req.village() != null || req.district() != null || req.city() != null || req.postalCode() != null) {
                    Address updatedAddress = new Address(
                            user.address() != null ? user.address().addressId() : null, // Gunakan ID alamat yang sudah ada
                            updatedUser.userId(), // ID user baru
                            req.street() != null ? req.street() : (user.address() != null ? user.address().street() : null),
                            req.rt() != null ? req.rt() : (user.address() != null ? user.address().rt() : null),
                            req.rw() != null ? req.rw() : (user.address() != null ? user.address().rw() : null),
                            req.village() != null ? req.village() : (user.address() != null ? user.address().village() : null),
                            req.district() != null ? req.district() : (user.address() != null ? user.address().district() : null),
                            req.city() != null ? req.city() : (user.address() != null ? user.address().city() : null),
                            req.postalCode() != null ? req.postalCode() : (user.address() != null ? user.address().postalCode() : null)
                    );

                    // Perbarui alamat di database
                    if (addressRepository.updateAddress(updatedAddress)) {
                        return Response.create("07", "00", "Profil berhasil diupdate", null);
                    } else {
                        return Response.create("07", "03", "Gagal mengupdate alamat", null);
                    }
                }
                return Response.create("07", "00", "Profil berhasil diupdate", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate profil", null);
            }
        });
    }
}
