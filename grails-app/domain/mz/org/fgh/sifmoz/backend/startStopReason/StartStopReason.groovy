package mz.org.fgh.sifmoz.backend.startStopReason

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class StartStopReason extends BaseEntity {
    public static final String TERMINO_DO_TRATAMENTO = "TERMINO_DO_TRATAMENTO"
    public static final String TRANSFERIDO_DE = "TRANSFERIDO_DE"
    public static final String REINICIO_TRATAMETO = "REINICIO_TRATAMETO"
    public static final String ABANDONO = "ABANDONO"
    public static final String TRANSFERIDO_PARA = "TRANSFERIDO_PARA"
    public static final String INICIO_MATERNIDADE = "INICIO_MATERNIDADE"
    public static final String VOLTOU_REFERENCIA = "VOLTOU_REFERENCIA"
    public static final String REFERIDO_PARA = "REFERIDO_PARA"
    public static final String TRANSITO = "TRANSITO"
    public static final String OBITO = "OBITO"
    public static final String ALTERACAO = "ALTERACAO"
    public static final String MANUNTENCAO = "MANUNTENCAO"
    public static final String INICIO_AO_TRATAMENTO = "INICIO_AO_TRATAMENTO"
    public static final String NOVO_PACIENTE = "NOVO_PACIENTE"
    public static final String INICIO_CCR = "INICIO_CCR"
    public static final String FIM_PPE = "FIM_PPE"
    public static final String TSPC = "TSPC"
    public static final String VOLTOU_A_SER_REFERIDO_PARA = "VOLTOU_A_SER_REFERIDO_PARA"
    public static final String REFERIDO_SECTOR_CLINICO = "REFERIDO_SECTOR_CLINICO"
    public static final String OUTRO = "OUTRO"
    public static final String REFERIDO_DC = "REFERIDO_DC"

    String id
    boolean isStartReason
    String code
    String reason

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_StartStopReason_Idx'
        datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        reason unique: true
    }

    boolean isNew() {
        return this.code.equals(INICIO_AO_TRATAMENTO) || this.code.equals(NOVO_PACIENTE)
    }

    boolean isManutencao() {
        return this.code.equals(MANUNTENCAO)
    }
    boolean isAlteracao() {
        return this.code.equals(ALTERACAO)
    }
    boolean isTransferido() {
        return this.code.equals(TRANSFERIDO_DE)
    }
    boolean isTransito() {
        return this.code.equals(TRANSITO)
    }
    boolean isReferido() {
        return this.code.equals(REFERIDO_PARA)
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode,administrationMenuCode,homeMenuCode))
        }
        return menus
    }
}
