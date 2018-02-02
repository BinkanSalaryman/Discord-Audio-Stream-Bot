using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DASB {
    public delegate void CopyDelegate<SRC, DST>(SRC source, int sourceOffset, DST destination, int destinationOffse, int length);
}
