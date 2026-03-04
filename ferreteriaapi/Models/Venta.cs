namespace ferreteriaapi.Models
{
    public class Venta
    {
        public int Id { get; set; }
        public DateOnly? Fecha { get; set; }
        public int? IdProducto { get; set; }
        public int? CantidadVendida { get; set; }
        public decimal? Total { get; set; }
    }
}
