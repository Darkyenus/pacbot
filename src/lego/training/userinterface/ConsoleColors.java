package lego.training.userinterface;

/**
 * Created by jIRKA on 4.10.2014.
 */
public enum ConsoleColors {

    DEFAULT{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','9','m'};
        }
    },


    RED{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','1','m'};
        }
    },
    BRIGHT_RED{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','1',';','3','1','m'};
        }
    },
    GREEN{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','2','m'};
        }
    },
    BRIGHT_GREEN{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','1',';','3','2','m'};
        }
    },
    YELLOW{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','3','m'};
        }
    },
    BRIGHT_YELLOW{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','1',';','3','3','m'};
        }
    },
    BLUE{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','4','m'};
        }
    },
    BRIGHT_BLUE{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','1',';','3','4','m'};
        }
    },
    MAGENTA{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','5','m'};
        }
    },
    BRIGHT_MAGENTA{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','1',';','3','5','m'};
        }
    },
    CYAN{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','6','m'};
        }
    },
    BRIGHT_CYAN{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','1',';','3','6','m'};
        }
    },
    WHITE{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','0',';','3','7','m'};
        }
    },
    BRIGHT_WHITE{
        @Override
        public byte[] getCode() {
            return new byte[]{27,'[','1',';','3','7','m'};
        }
    };

    public abstract byte[] getCode();

}
